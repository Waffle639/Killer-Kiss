package org.example.service;

import org.example.KillerKiss;
import org.example.Persona;
import org.example.repository.KillerKissRepository;
import org.example.repository.PersonaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service que contiene la lógica de negocio para gestionar Partidas de Killer
 * Kiss.
 */
@Service
@Transactional
public class KillerKissService {

    @Autowired
    private KillerKissRepository partidaRepository;

    @Autowired
    private PersonaRepository personaRepository;

    @Autowired
    private PersonaService personaService;

    @Autowired
    private GmailService gmailService;

    /**
     * Obtiene todas las partidas.
     */
    public List<KillerKiss> listarTodas() {
        return partidaRepository.findAll();
    }

    /**
     * Obtiene todas las partidas activas.
     */
    public List<KillerKiss> listarActivas() {
        return partidaRepository.findByEstatTrue();
    }

    /**
     * Obtiene todas las partidas finalizadas.
     */
    public List<KillerKiss> listarFinalizadas() {
        return partidaRepository.findByEstatFalse();
    }

    /**
     * Busca una partida por ID.
     */
    public Optional<KillerKiss> buscarPorId(Long id) {
        return partidaRepository.findById(id);
    }

    /**
     * Crea una nueva partida con validaciones.
     */
    public KillerKiss crear(KillerKiss partida) {
        // Validación: nombre obligatorio
        if (partida.getNom() == null || partida.getNom().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de la partida es obligatorio");
        }

        // Validación: mínimo 2 jugadores
        if (partida.getPersonas() == null || partida.getPersonas().size() < 2) {
            throw new IllegalArgumentException("Se necesitan al menos 2 jugadores para crear una partida");
        }

        // Validación: no puede haber otra partida activa con el mismo nombre
        if (partidaRepository.existsByNomAndEstatTrue(partida.getNom())) {
            throw new IllegalArgumentException("Ya existe una partida activa con ese nombre");
        }

        // IMPORTANTE: Cargar las personas completas desde la base de datos
        List<Persona> personasCompletas = new java.util.ArrayList<>();
        for (Persona p : partida.getPersonas()) {
            Persona personaCompleta = personaRepository.findById(p.getId())
                .orElseThrow(() -> new RuntimeException("Persona no encontrada con ID: " + p.getId()));
            personasCompletas.add(personaCompleta);
        }
        partida.setPersonas(personasCompletas);

        // Establecer fecha de creación
        partida.setFechaCreacion(java.time.LocalDateTime.now());

        // Mezclar y asignar víctimas
        partida.sortPersonas();

        // Iniciar la partida
        partida.iniciarPartida();

        // Guardar en base de datos
        KillerKiss partidaGuardada = partidaRepository.save(partida);

        // NO enviar correos aquí - se enviarán desde el endpoint separado después

        return partidaGuardada;
    }

    /**
     * Finaliza una partida y declara un ganador.
     */
    public KillerKiss finalizarPartida(Long partidaId, Long ganadorId) {
        // Validar que la partida existe
        KillerKiss partida = partidaRepository.findById(partidaId)
                .orElseThrow(() -> new RuntimeException("Partida no encontrada con ID: " + partidaId));

        // Validar que la partida está activa
        if (!partida.isEstat()) {
            throw new IllegalStateException("La partida ya está finalizada");
        }

        // Validar que el ganador existe
        Persona ganador = personaRepository.findById(ganadorId)
                .orElseThrow(() -> new RuntimeException("Ganador no encontrado con ID: " + ganadorId));

        // Validar que el ganador está en la partida
        if (!partida.getPersonas().contains(ganador)) {
            throw new IllegalArgumentException("El ganador no está participando en esta partida");
        }

        // Sumar victoria al ganador
        personaService.sumarVictoria(ganadorId);

        // Finalizar partida
        partida.acavarPartida(ganador);

        // Guardar cambios
        KillerKiss partidaFinalizada = partidaRepository.save(partida);

        return partidaFinalizada;
    }

    /**
     * Elimina una partida.
     */
    public void eliminar(Long id) {
        if (!partidaRepository.existsById(id)) {
            throw new RuntimeException("Partida no encontrada con ID: " + id);
        }
        partidaRepository.deleteById(id);
    }

    /**
     * Obtiene estadísticas generales.
     */
    public EstadisticasDTO obtenerEstadisticas() {
        long totalPartidas = partidaRepository.count();
        long partidasActivas = partidaRepository.countByEstat(true);
        long partidasFinalizadas = partidaRepository.countByEstat(false);
        long totalJugadores = personaRepository.count();

        return new EstadisticasDTO(totalPartidas, partidasActivas, partidasFinalizadas, totalJugadores);
    }

    /**
     * Envía los correos con las asignaciones a todos los participantes
     * usando Gmail SMTP directamente desde el backend.
     */
    public ResultadoEnvioDTO enviarCorreosPartida(KillerKiss partida, String idioma) {
        ResultadoEnvioDTO resultado = new ResultadoEnvioDTO();
        List<Persona> participantes = partida.getPersonas();
        Map<String, String> asignaciones = new HashMap<>();

        for (int i = 0; i < participantes.size(); i++) {
            Persona jugador = participantes.get(i);
            Persona victima = participantes.get((i + 1) % participantes.size());

            String nombreJugador = jugador.getNom() != null ? jugador.getNom() : "Jugador";
            String nombreVictima = victima.getNom() != null ? victima.getNom() : "Desconocido";

            if (jugador.getMail() == null || jugador.getMail().isEmpty()) {
                resultado.agregarResultado(nombreJugador, "Sin email", false, "No tiene email", nombreVictima);
                continue;
            }

            String subject = idioma.equals("ca") ? "La teva missio a Killer Kiss" : "Tu mision en Killer Kiss";
            String html = buildEmailHtml(nombreJugador, nombreVictima, idioma);

            boolean enviado;
            String mensajeError;

            if (!gmailService.isConfigured()) {
                enviado = false;
                mensajeError = "Gmail no configurado - crea mail.config con gmail.username y gmail.password";
            } else {
                enviado = gmailService.sendEmail(jugador.getMail(), subject, html);
                mensajeError = "Error al enviar";
            }

            if (enviado) {
                asignaciones.put(jugador.getMail(), nombreVictima);
                resultado.agregarResultado(nombreJugador, jugador.getMail(), true, "Enviado correctamente", nombreVictima);
            } else {
                resultado.agregarResultado(nombreJugador, jugador.getMail(), false, mensajeError, nombreVictima);
            }
        }

        partida.setAsignaciones(asignaciones);
        partidaRepository.save(partida);

        resultado.setPartidaId(partida.getId());
        return resultado;
    }

    /**
     * Reenvia un correo individual a un jugador concreto.
     */
    public boolean reenviarCorreoIndividual(Long partidaId, String email, String idioma) {
        KillerKiss partida = partidaRepository.findById(partidaId)
                .orElseThrow(() -> new RuntimeException("Partida no encontrada"));

        String victimaNombre = partida.getAsignaciones().get(email);
        if (victimaNombre == null) {
            return false;
        }

        Persona jugador = partida.getPersonas().stream()
                .filter(p -> email.equals(p.getMail()))
                .findFirst().orElse(null);

        String nombre = jugador != null ? jugador.getNom() : "Jugador";
        String subject = idioma.equals("ca") ? "La teva missio a Killer Kiss" : "Tu mision en Killer Kiss";
        String html = buildEmailHtml(nombre, victimaNombre, idioma);
        return gmailService.sendEmail(email, subject, html);
    }

    private String buildEmailHtml(String nombreJugador, String nombreVictima, String idioma) {
        String titulo = idioma.equals("ca") ? "La teva missio a Killer Kiss" : "Tu mision en Killer Kiss";
        String saludo = idioma.equals("ca") ? "Hola" : "Hola";
        String mensaje = idioma.equals("ca")
                ? "Estas participant en una partida de <strong>Killer Kiss</strong>!"
                : "Estas participando en una partida de <strong>Killer Kiss</strong>!";
        String tuVictima = idioma.equals("ca") ? "La teva victima es:" : "Tu victima es:";
        String instruccion = idioma.equals("ca")
                ? "Has de fer un peto a la teva victima sense que et vegin. Bona sort!"
                : "Debes besar a tu victima sin que te vean. Buena suerte!";

        return "<div style=\"font-family:'Segoe UI',Tahoma,Geneva,Verdana,sans-serif;max-width:500px;margin:0 auto\">"
                + "<div style=\"background:linear-gradient(135deg,#667eea,#764ba2);padding:30px;border-radius:15px 15px 0 0;text-align:center\">"
                + "<h1 style=\"color:white;margin:0;font-size:28px\">" + titulo + "</h1>"
                + "</div>"
                + "<div style=\"background:white;padding:30px;border-radius:0 0 15px 15px;box-shadow:0 4px 15px rgba(0,0,0,0.1)\">"
                + "<p style=\"font-size:16px\">" + saludo + " <strong>" + nombreJugador + "</strong>,</p>"
                + "<p style=\"font-size:16px\">" + mensaje + "</p>"
                + "<div style=\"background:#f8f4ff;border-left:4px solid #764ba2;padding:15px;margin:20px 0;border-radius:8px\">"
                + "<p style=\"margin:0;color:#666;font-size:14px\">" + tuVictima + "</p>"
                + "<h2 style=\"margin:5px 0;color:#764ba2;font-size:32px\">" + nombreVictima + "</h2>"
                + "</div>"
                + "<p style=\"font-size:14px;color:#888\">" + instruccion + "</p>"
                + "</div></div>";
    }

    /**
     * Clase interna para devolver estadísticas.
     */
    public static class EstadisticasDTO {

        private long totalPartidas;
        private long partidasActivas;
        private long partidasFinalizadas;
        private long totalJugadores;

        public EstadisticasDTO(long totalPartidas, long partidasActivas, long partidasFinalizadas, long totalJugadores) {
            this.totalPartidas = totalPartidas;
            this.partidasActivas = partidasActivas;
            this.partidasFinalizadas = partidasFinalizadas;
            this.totalJugadores = totalJugadores;
        }

        // Getters
        public long getTotalPartidas() {
            return totalPartidas;
        }

        public long getPartidasActivas() {
            return partidasActivas;
        }

        public long getPartidasFinalizadas() {
            return partidasFinalizadas;
        }

        public long getTotalJugadores() {
            return totalJugadores;
        }
    }

    /**
     * Clase interna para devolver resultado de envío de emails.
     */
    public static class ResultadoEnvioDTO {

        private Long partidaId;
        private List<DetalleEnvio> detalles = new java.util.ArrayList<>();
        private int exitosos = 0;
        private int fallidos = 0;

        public void agregarResultado(String nombre, String email, boolean exitoso, String mensaje, String victima) {
            detalles.add(new DetalleEnvio(nombre, email, exitoso, mensaje, victima));
            if (exitoso) {
                exitosos++;
            } else {
                fallidos++;
            }
        }
        
        public void setPartidaId(Long partidaId) {
            this.partidaId = partidaId;
        }
        
        public Long getPartidaId() {
            return partidaId;
        }

        public List<DetalleEnvio> getDetalles() {
            return detalles;
        }

        public int getExitosos() {
            return exitosos;
        }

        public int getFallidos() {
            return fallidos;
        }

        public int getTotal() {
            return exitosos + fallidos;
        }

        public static class DetalleEnvio {

            private String nombre;
            private String email;
            private boolean exitoso;
            private String mensaje;
            private String victima; // nombre de la víctima/objetivo para EmailJS

            public DetalleEnvio(String nombre, String email, boolean exitoso, String mensaje, String victima) {
                this.nombre = nombre;
                this.email = email;
                this.exitoso = exitoso;
                this.mensaje = mensaje;
                this.victima = victima;
            }

            public String getNombre() {
                return nombre;
            }

            public String getEmail() {
                return email;
            }

            public boolean isExitoso() {
                return exitoso;
            }

            public String getMensaje() {
                return mensaje;
            }

            public String getVictima() {
                return victima;
            }
        }
    }
}
