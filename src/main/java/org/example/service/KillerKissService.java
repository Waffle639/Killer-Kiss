package org.example.service;

import org.example.KillerKiss;
import org.example.Persona;
import org.example.repository.KillerKissRepository;
import org.example.repository.PersonaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

    @Autowired(required = false)
    private SendGridApiService sendGridApiService;

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
        boolean apiDisponible = sendGridApiService != null && sendGridApiService.isConfigured();
        System.out.println("📧 Email: " + (apiDisponible ? "SendGrid API" : "SMTP") + " - Remitente: " + mailRemitente);
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
     * @param idioma El idioma en el que enviar los correos ('es' o 'ca')
     */
    public ResultadoEnvioDTO enviarEmailsInicioPartida(KillerKiss partida, String idioma) {
        ResultadoEnvioDTO resultado = new ResultadoEnvioDTO();
        List<Persona> participantes = partida.getPersonas();
        Map<String, String> asignaciones = new HashMap<>();

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
            
            // Construir mensaje HTML según idioma recibido como parámetro
            String asunto, mensaje;
            if ("ca".equals(idioma)) {
                asunto = "Killer Kiss - El teu objectiu";
                mensaje = "<!DOCTYPE html>"
                        + "<html lang='ca'>"
                        + "<head><meta charset='UTF-8'><meta name='viewport' content='width=device-width, initial-scale=1.0'></head>"
                        + "<body style='margin: 0; padding: 0; font-family: Arial, sans-serif; background-color: #f4f4f4;'>"
                        + "<div style='max-width: 600px; margin: 20px auto; background-color: #ffffff; border-radius: 8px; overflow: hidden; box-shadow: 0 2px 8px rgba(0,0,0,0.1);'>"
                        + "<div style='background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 30px; text-align: center;'>"
                        + "<h1 style='margin: 0; font-size: 28px; letter-spacing: 1px;'>💋 KILLER KISS</h1>"
                        + "<p style='margin: 10px 0 0 0; font-size: 14px; opacity: 0.9;'>El joc de l'assassí</p>"
                        + "</div>"
                        + "<div style='padding: 40px 30px;'>"
                        + "<p style='font-size: 16px; color: #333; margin: 0 0 20px 0;'>Hola <strong>" + nombreJugador + "</strong>,</p>"
                        + "<div style='background-color: #f8f9fa; border-left: 4px solid #667eea; padding: 20px; margin: 20px 0; border-radius: 4px;'>"
                        + "<p style='margin: 0; font-size: 18px; color: #333;'><strong>El teu objectiu és:</strong></p>"
                        + "<p style='margin: 10px 0 0 0; font-size: 24px; color: #667eea; font-weight: bold;'>" + nombreVictima + "</p>"
                        + "</div>"
                        + "<p style='font-size: 15px; color: #666; margin: 20px 0 0 0; line-height: 1.6;'>Bona sort i que guanyi el millor! 🍀</p>"
                        + "</div>"
                        + "<div style='background-color: #f8f9fa; padding: 20px; text-align: center; color: #999; font-size: 12px;'>"
                        + "<p style='margin: 0;'>Killer Kiss - El joc de l'assassí</p>"
                        + "</div>"
                        + "</div>"
                        + "</body>"
                        + "</html>";
            } else {
                asunto = "Killer Kiss - Tu objetivo";
                mensaje = "<!DOCTYPE html>"
                        + "<html lang='es'>"
                        + "<head><meta charset='UTF-8'><meta name='viewport' content='width=device-width, initial-scale=1.0'></head>"
                        + "<body style='margin: 0; padding: 0; font-family: Arial, sans-serif; background-color: #f4f4f4;'>"
                        + "<div style='max-width: 600px; margin: 20px auto; background-color: #ffffff; border-radius: 8px; overflow: hidden; box-shadow: 0 2px 8px rgba(0,0,0,0.1);'>"
                        + "<div style='background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 30px; text-align: center;'>"
                        + "<h1 style='margin: 0; font-size: 28px; letter-spacing: 1px;'>💋 KILLER KISS</h1>"
                        + "<p style='margin: 10px 0 0 0; font-size: 14px; opacity: 0.9;'>El juego del asesino</p>"
                        + "</div>"
                        + "<div style='padding: 40px 30px;'>"
                        + "<p style='font-size: 16px; color: #333; margin: 0 0 20px 0;'>Hola <strong>" + nombreJugador + "</strong>,</p>"
                        + "<div style='background-color: #f8f9fa; border-left: 4px solid #667eea; padding: 20px; margin: 20px 0; border-radius: 4px;'>"
                        + "<p style='margin: 0; font-size: 18px; color: #333;'><strong>Tu objetivo es:</strong></p>"
                        + "<p style='margin: 10px 0 0 0; font-size: 24px; color: #667eea; font-weight: bold;'>" + nombreVictima + "</p>"
                        + "</div>"
                        + "<p style='font-size: 15px; color: #666; margin: 20px 0 0 0; line-height: 1.6;'>¡Buena suerte y que gane el mejor! 🍀</p>"
                        + "</div>"
                        + "<div style='background-color: #f8f9fa; padding: 20px; text-align: center; color: #999; font-size: 12px;'>"
                        + "<p style='margin: 0;'>Killer Kiss - El juego del asesino</p>"
                        + "</div>"
                        + "</div>"
                        + "</body>"
                        + "</html>";
            }

            boolean enviado = enviarCorreu(jugador.getMail(), mensaje, asunto);
            
            // Solo guardar asignación si el correo falló (para poder reenviar después)
            if (!enviado) {
                asignaciones.put(jugador.getMail(), victima.getMail());
            }
            
            resultado.agregarResultado(nombreJugador, jugador.getMail(), enviado,
                    enviado ? "Enviado correctamente" : "Error al enviar");
        }

        // Guardar solo las asignaciones de correos fallidos en la partida
        partida.setAsignaciones(asignaciones);
        partidaRepository.save(partida);
        
        resultado.setPartidaId(partida.getId());

        return resultado;
    }
    
    /**
     * Reenvía el correo a un jugador específico de una partida activa.
     */
    public Map<String, Object> reenviarCorreoJugador(Long partidaId, String emailJugador) {
        Map<String, Object> respuesta = new HashMap<>();
        
        Optional<KillerKiss> partidaOpt = partidaRepository.findById(partidaId);
        if (partidaOpt.isEmpty()) {
            respuesta.put("exito", false);
            respuesta.put("mensaje", "Partida no encontrada");
            return respuesta;
        }
        
        KillerKiss partida = partidaOpt.get();
        
        // Verificar que la partida tiene asignaciones guardadas
        if (partida.getAsignaciones() == null || partida.getAsignaciones().isEmpty()) {
            respuesta.put("exito", false);
            respuesta.put("mensaje", "La partida no tiene asignaciones guardadas");
            return respuesta;
        }
        
        // Obtener email del objetivo
        String emailObjetivo = partida.getAsignaciones().get(emailJugador);
        if (emailObjetivo == null) {
            respuesta.put("exito", false);
            respuesta.put("mensaje", "No se encontró asignación para este jugador");
            return respuesta;
        }
        
        // Buscar personas
        Optional<Persona> jugadorOpt = personaRepository.findByMail(emailJugador);
        Optional<Persona> objetivoOpt = personaRepository.findByMail(emailObjetivo);
        
        if (jugadorOpt.isEmpty() || objetivoOpt.isEmpty()) {
            respuesta.put("exito", false);
            respuesta.put("mensaje", "Jugador u objetivo no encontrado");
            return respuesta;
        }
        
        Persona jugador = jugadorOpt.get();
        Persona objetivo = objetivoOpt.get();
        
        String nombreJugador = jugador.getNom() != null ? jugador.getNom() : "Jugador";
        String nombreObjetivo = objetivo.getNom() != null ? objetivo.getNom() : "Desconocido";
        
        String asunto = "Partida Killer Kiss: " + partida.getNom();
        String mensaje = "Hola " + nombreJugador + ",\n\n"
                + "Bienvenido a la partida de Killer Kiss!\n\n"
                + "Tu victima es: " + nombreObjetivo + "\n"
                + "Buena suerte!";
        
        boolean enviado = enviarCorreu(emailJugador, mensaje, asunto);
        
        // Si se envió correctamente, eliminar la asignación del mapa
        if (enviado) {
            partida.getAsignaciones().remove(emailJugador);
            partidaRepository.save(partida);
        }
        
        respuesta.put("exito", enviado);
        respuesta.put("mensaje", enviado ? "Correo reenviado correctamente" : "Error al reenviar correo");
        respuesta.put("jugador", nombreJugador);
        respuesta.put("email", emailJugador);
        
        return respuesta;
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
        
        // Validación básica del email
        if (destinatari == null || destinatari.trim().isEmpty()) {
            System.err.println("✗ Email vacío o nulo");
            return false;
        }
        
        String emailTrimmed = destinatari.trim();
        
        // Validación de formato básico de email
        if (!emailTrimmed.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            System.err.println("✗ Email con formato inválido: " + emailTrimmed);
            return false;
        }

        try {
            boolean exito = false;
            
            if (sendGridApiService != null && sendGridApiService.isConfigured()) {
                // SendGrid API devuelve true/false según el resultado real
                exito = sendGridApiService.sendEmail(mailRemitente, destinatari, assumpte, missatge);
                if (exito) {
                    System.out.println("✓ Email enviado via SendGrid a " + destinatari);
                } else {
                    System.err.println("✗ SendGrid rechazó el email a " + destinatari);
                }
            } else {
                // SMTP - intentar enviar y capturar cualquier error
                try {
                    jakarta.mail.internet.MimeMessage mimeMessage = mailSender.createMimeMessage();
                    org.springframework.mail.javamail.MimeMessageHelper helper = 
                        new org.springframework.mail.javamail.MimeMessageHelper(mimeMessage, true, "UTF-8");
                    helper.setFrom(mailRemitente);
                    helper.setTo(destinatari);
                    helper.setSubject(assumpte);
                    helper.setText(missatge, true); // true = HTML
                    mailSender.send(mimeMessage);
                    exito = true;
                    System.out.println("✓ Email enviado via SMTP a " + destinatari);
                } catch (org.springframework.mail.MailException | jakarta.mail.MessagingException e) {
                    System.err.println("✗ Error SMTP enviando a " + destinatari + ": " + e.getMessage());
                    exito = false;
                }
            }
            
            if (exito) {
                emailCounter.incrementar();
            }
            return exito;

        } catch (Exception e) {
            System.err.println("✗ Error inesperado enviando email a " + destinatari + ": " + e.getMessage());
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

        private Long partidaId;
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
