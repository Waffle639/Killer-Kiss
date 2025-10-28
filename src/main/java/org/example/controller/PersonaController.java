package org.example.controller;

import org.example.Persona;
import org.example.service.PersonaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller REST para gestionar Personas.
 * Expone endpoints HTTP para que el frontend pueda comunicarse con el backend.
 */
@RestController
@RequestMapping("/api/personas")
@CrossOrigin(origins = "*") // Permite peticiones desde cualquier origen (para desarrollo)
public class PersonaController {

    @Autowired
    private PersonaService personaService;

    /**
     * GET /api/personas
     * Obtiene todas las personas.
     */
    @GetMapping
    public ResponseEntity<List<Persona>> listarTodas() {
        List<Persona> personas = personaService.listarTodas();
        return ResponseEntity.ok(personas);
    }

    /**
     * GET /api/personas/{id}
     * Obtiene una persona por su ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Persona> buscarPorId(@PathVariable(name = "id") Long id) {
        return personaService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * GET /api/personas/ranking
     * Obtiene el ranking de personas ordenadas por victorias.
     */
    @GetMapping("/ranking")
    public ResponseEntity<List<Persona>> obtenerRanking() {
        List<Persona> ranking = personaService.obtenerRanking();
        return ResponseEntity.ok(ranking);
    }

    /**
     * POST /api/personas
     * Crea una nueva persona.
     * Body JSON: {"nom":"Juan","mail":"juan@mail.com"}
     */
    @PostMapping
    public ResponseEntity<?> crear(@RequestBody Persona persona) {
        try {
            Persona personaCreada = personaService.crear(persona);
            return ResponseEntity.status(HttpStatus.CREATED).body(personaCreada);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error al crear persona: " + e.getMessage()));
        }
    }

    /**
     * PUT /api/personas/{id}
     * Actualiza una persona existente.
     * Body JSON: {"nom":"Juan Actualizado","mail":"nuevo@mail.com"}
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable(name = "id") Long id, @RequestBody Persona persona) {
        try {
            Persona personaActualizada = personaService.actualizar(id, persona);
            return ResponseEntity.ok(personaActualizada);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error al actualizar persona: " + e.getMessage()));
        }
    }

    /**
     * DELETE /api/personas/{id}
     * Elimina una persona.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable(name = "id") Long id) {
        try {
            personaService.eliminar(id);
            return ResponseEntity.ok(new MessageResponse("Persona eliminada correctamente"));
        } catch (IllegalArgumentException e) {
            // Error de validación (persona en partidas)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage()));
        } catch (RuntimeException e) {
            // Persona no encontrada
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error al eliminar persona: " + e.getMessage()));
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
