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
     * Calcula las asignaciones de la partida (jugador → víctima)
     * y devuelve la lista para que el frontend envíe los emails via EmailJS.
     * @param idioma Parámetro mantenido por compatibilidad (no usado en backend)
     */
    public ResultadoEnvioDTO obtenerAsignacionesPartida(KillerKiss partida, String idioma) {
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

            // Guardar asignación (email jugador → nombre víctima) para posible reenvío
            asignaciones.put(jugador.getMail(), nombreVictima);
            resultado.agregarResultado(nombreJugador, jugador.getMail(), true, "Listo para enviar via EmailJS", nombreVictima);
        }

        partida.setAsignaciones(asignaciones);
        partidaRepository.save(partida);

        resultado.setPartidaId(partida.getId());
        return resultado;
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
