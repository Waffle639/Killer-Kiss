package org.example.service;

import org.example.KillerKiss;
import org.example.Persona;
import org.example.repository.KillerKissRepository;
import org.example.repository.PersonaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;
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
    private JavaMailSender mailSender;

    @Autowired
    private EmailCounter emailCounter;

    // Email remitente: En producción (SendGrid) usará MAIL_FROM, en local usará mail.remitente
    @Value("${spring.mail.from:${mail.remitente:}}")
    private String mailRemitente;

    @Value("${spring.mail.host:unknown}")
    private String mailHost;

    @Value("${spring.mail.port:0}")
    private int mailPort;

    @Value("${spring.mail.username:unknown}")
    private String mailUsername;

    @PostConstruct
    public void init() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("📧 CONFIGURACIÓN EMAIL INICIADA");
        System.out.println("=".repeat(80));
        System.out.println("📌 Host: " + mailHost);
        System.out.println("📌 Puerto: " + mailPort);
        System.out.println("📌 Usuario: " + mailUsername);
        System.out.println("📌 Remitente: " + mailRemitente);
        System.out.println("📌 MailSender configurado: " + (mailSender != null ? "✓ SÍ" : "✗ NO"));
        System.out.println("=".repeat(80) + "\n");
    }

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
     * Envía emails a todos los participantes al inicio de la partida. Retorna
     * un resumen del estado de envío.
     */
    public ResultadoEnvioDTO enviarEmailsInicioPartida(KillerKiss partida) {
        ResultadoEnvioDTO resultado = new ResultadoEnvioDTO();
        List<Persona> participantes = partida.getPersonas();

        for (int i = 0; i < participantes.size(); i++) {
            Persona jugador = participantes.get(i);
            Persona victima = participantes.get((i + 1) % participantes.size());

            if (jugador.getMail() == null || jugador.getMail().isEmpty()) {
                System.err.println("Jugador sin email: " + (jugador.getNom() != null ? jugador.getNom() : "ID " + jugador.getId()));
                resultado.agregarResultado(
                    jugador.getNom() != null ? jugador.getNom() : "Sin nombre", 
                    "Sin email", 
                    false, 
                    "No tiene email"
                );
                continue;
            }

            String nombreJugador = jugador.getNom() != null ? jugador.getNom() : "Jugador";
            String nombreVictima = victima.getNom() != null ? victima.getNom() : "Desconocido";
            
            String asunto = "Partida Killer Kiss: " + partida.getNom();
            String mensaje = "Hola " + nombreJugador + ",\n\n"
                    + "Bienvenido a la partida de Killer Kiss!\n\n"
                    + "Tu victima es: " + nombreVictima + "\n"
                    + "Buena suerte!";

            boolean enviado = enviarCorreu(jugador.getMail(), mensaje, asunto);
            resultado.agregarResultado(nombreJugador, jugador.getMail(), enviado,
                    enviado ? "Enviado correctamente" : "Error al enviar");
        }

        return resultado;
    }

    /**
     * Método auxiliar para enviar correos electrónicos usando JavaMailSender.
     * Retorna true si se envió correctamente, false si hubo error.
     */
    private boolean enviarCorreu(String destinatari, String missatge, String assumpte) {
        // Verificar si se ha alcanzado el límite diario
        if (emailCounter.limiteAlcanzado()) {
            System.err.println("✗ Límite diario de emails alcanzado (" + emailCounter.getLimiteDiario() + ")");
            return false;
        }

        try {
            System.out.println("🔧 [DEBUG] Configuración email:");
            System.out.println("  - Remitente configurado: " + mailRemitente);
            System.out.println("  - Destinatario: " + destinatari);
            System.out.println("  - Asunto: " + assumpte);
            
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(mailRemitente);
            message.setTo(destinatari);
            message.setSubject(assumpte);
            message.setText(missatge);

            System.out.println("📧 [DEBUG] Intentando enviar email vía mailSender...");
            mailSender.send(message);
            
            emailCounter.incrementar();
            System.out.println("✓ Correo enviado a " + destinatari + " correctamente [" 
                + emailCounter.getContadorFormateado() + " emails hoy]");
            return true;

        } catch (Exception e) {
            System.err.println("✗ ERROR al enviar correo a " + destinatari);
            System.err.println("  - Tipo de excepción: " + e.getClass().getName());
            System.err.println("  - Mensaje: " + e.getMessage());
            System.err.println("  - Causa raíz: " + (e.getCause() != null ? e.getCause().getMessage() : "N/A"));
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Obtiene el contador de emails enviados hoy.
     */
    public Map<String, Object> getContadorEmails() {
        Map<String, Object> contador = new HashMap<>();
        contador.put("enviados", emailCounter.getEnviadosHoy());
        contador.put("limite", emailCounter.getLimiteDiario());
        contador.put("formateado", emailCounter.getContadorFormateado());
        return contador;
    }

    /**
     * Obtiene estadísticas de emails.
     */
    public Map<String, Object> getEstadisticasEmails() {
        return getContadorEmails();
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

        private List<DetalleEnvio> detalles = new java.util.ArrayList<>();
        private int exitosos = 0;
        private int fallidos = 0;

        public void agregarResultado(String nombre, String email, boolean exitoso, String mensaje) {
            detalles.add(new DetalleEnvio(nombre, email, exitoso, mensaje));
            if (exitoso) {
                exitosos++;
            } else {
                fallidos++;
            }
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

            public DetalleEnvio(String nombre, String email, boolean exitoso, String mensaje) {
                this.nombre = nombre;
                this.email = email;
                this.exitoso = exitoso;
                this.mensaje = mensaje;
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
        }
    }
}
