package org.example.controller;

import org.example.KillerKiss;
import org.example.service.KillerKissService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controller REST para gestionar Partidas de Killer Kiss.
 * Expone endpoints HTTP para que el frontend pueda comunicarse con el backend.
 */
@RestController
@RequestMapping("/api/partidas")
@CrossOrigin(origins = "*") // Permite peticiones desde cualquier origen (para desarrollo)
public class KillerKissController {

    @Autowired
    private KillerKissService partidaService;

    /**
     * GET /api/partidas
     * Obtiene todas las partidas.
     */
    @GetMapping
    public ResponseEntity<List<KillerKiss>> listarTodas() {
        List<KillerKiss> partidas = partidaService.listarTodas();
        return ResponseEntity.ok(partidas);
    }

    /**
     * GET /api/partidas/activas
     * Obtiene solo las partidas activas.
     */
    @GetMapping("/activas")
    public ResponseEntity<List<KillerKiss>> listarActivas() {
        List<KillerKiss> partidas = partidaService.listarActivas();
        return ResponseEntity.ok(partidas);
    }

    /**
     * GET /api/partidas/finalizadas
     * Obtiene solo las partidas finalizadas.
     */
    @GetMapping("/finalizadas")
    public ResponseEntity<List<KillerKiss>> listarFinalizadas() {
        List<KillerKiss> partidas = partidaService.listarFinalizadas();
        return ResponseEntity.ok(partidas);
    }

    /**
     * GET /api/partidas/{id}
     * Obtiene una partida por su ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<KillerKiss> buscarPorId(@PathVariable Long id) {
        return partidaService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * GET /api/partidas/estadisticas
     * Obtiene estadísticas generales.
     */
    @GetMapping("/estadisticas")
    public ResponseEntity<KillerKissService.EstadisticasDTO> obtenerEstadisticas() {
        KillerKissService.EstadisticasDTO stats = partidaService.obtenerEstadisticas();
        return ResponseEntity.ok(stats);
    }

    /**
     * POST /api/partidas
     * Crea una nueva partida.
     * Body JSON: {"nom":"Partida 1","personas":[{"id":1},{"id":2}]}
     */
    @PostMapping
    public ResponseEntity<?> crear(@RequestBody KillerKiss partida) {
        try {
            KillerKiss partidaCreada = partidaService.crear(partida);
            return ResponseEntity.status(HttpStatus.CREATED).body(partidaCreada);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error al crear partida: " + e.getMessage()));
        }
    }

    /**
     * PUT /api/partidas/{id}/finalizar
     * Finaliza una partida y declara un ganador.
     * Body JSON: {"ganadorId": 5}
     */
    @PutMapping("/{id}/finalizar")
    public ResponseEntity<?> finalizar(@PathVariable Long id, @RequestBody Map<String, Long> body) {
        try {
            Long ganadorId = body.get("ganadorId");
            if (ganadorId == null) {
                return ResponseEntity.badRequest().body(new ErrorResponse("El campo 'ganadorId' es obligatorio"));
            }

            KillerKiss partidaFinalizada = partidaService.finalizarPartida(id, ganadorId);
            return ResponseEntity.ok(partidaFinalizada);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error al finalizar partida: " + e.getMessage()));
        }
    }

    /**
     * DELETE /api/partidas/{id}
     * Elimina una partida.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        try {
            partidaService.eliminar(id);
            return ResponseEntity.ok(new MessageResponse("Partida eliminada correctamente"));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error al eliminar partida: " + e.getMessage()));
        }
    }

    /**
     * Clase interna para respuestas de error.
     */
    public static class ErrorResponse {
        private String error;

        public ErrorResponse(String error) {
            this.error = error;
        }

        public String getError() {
            return error;
        }

        public void setError(String error) {
            this.error = error;
        }
    }

    /**
     * Clase interna para respuestas de mensaje.
     */
    public static class MessageResponse {
        private String message;

        public MessageResponse(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}
