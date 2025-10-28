package org.example.service;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.example.KillerKiss;
import org.example.Persona;
import org.example.repository.KillerKissRepository;
import org.example.repository.PersonaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Properties;

/**
 * Service que contiene la lógica de negocio para gestionar Partidas de Killer Kiss.
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

        // Mezclar y asignar víctimas
        partida.sortPersonas();
        
        // Iniciar la partida
        partida.iniciarPartida();

        // Guardar en base de datos
        KillerKiss partidaGuardada = partidaRepository.save(partida);

        // Enviar emails a los participantes
        enviarEmailsInicioPartida(partidaGuardada);

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

        // Enviar email de finalización
        enviarEmailFinalizacion(partidaFinalizada, ganador);

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
     * Envía emails a todos los participantes al inicio de la partida.
     */
    private void enviarEmailsInicioPartida(KillerKiss partida) {
        try {
            List<Persona> participantes = partida.getPersonas();
            for (int i = 0; i < participantes.size(); i++) {
                Persona jugador = participantes.get(i);
                Persona victima = participantes.get((i + 1) % participantes.size());

                String asunto = "¡Partida Killer Kiss: " + partida.getNom() + "!";
                String mensaje = String.format(
                    "Hola %s,\n\n" +
                    "¡Bienvenido a la partida de Killer Kiss!\n\n" +
                    "Tu víctima es: %s\n" +
                    "¡Buena suerte!\n\n" +
                    jugador.getNom(),
                    victima.getNom()
                );

                enviarCorreo(jugador.getMail(), asunto, mensaje);
            }
        } catch (Exception e) {
            System.err.println("Error al enviar emails de inicio: " + e.getMessage());
            // No lanzamos excepción para que no falle la creación de la partida
        }
    }

    /**
     * Envía email de finalización de partida.
     */
    private void enviarEmailFinalizacion(KillerKiss partida, Persona ganador) {
        try {
            for (Persona jugador : partida.getPersonas()) {
                String asunto = "Partida Killer Kiss Finalizada: " + partida.getNom();
                String mensaje = String.format(
                    "Hola %s,\n\n" +
                    "La partida '%s' ha finalizado.\n\n" +
                    "¡El ganador es: %s!\n\n" +
                    "Gracias por participar.\n\n" +
                    "Killer Kiss Team",
                    jugador.getNom(),
                    partida.getNom(),
                    ganador.getNom()
                );

                enviarCorreo(jugador.getMail(), asunto, mensaje);
            }
        } catch (Exception e) {
            System.err.println("Error al enviar emails de finalización: " + e.getMessage());
        }
    }

    /**
     * Método auxiliar para enviar correos electrónicos.
     */
    private void enviarCorreo(String destinatario, String asunto, String mensaje) {
        // TODO: Configurar con tus credenciales SMTP
        // Por ahora solo imprime en consola
        System.out.println("===== EMAIL =====");
        System.out.println("Para: " + destinatario);
        System.out.println("Asunto: " + asunto);
        System.out.println("Mensaje: " + mensaje);
        System.out.println("=================\n");
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
        public long getTotalPartidas() { return totalPartidas; }
        public long getPartidasActivas() { return partidasActivas; }
        public long getPartidasFinalizadas() { return partidasFinalizadas; }
        public long getTotalJugadores() { return totalJugadores; }
    }
}
