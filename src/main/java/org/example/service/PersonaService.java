package org.example.service;

import org.example.Persona;
import org.example.repository.PersonaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service que contiene la lógica de negocio para gestionar Personas.
 * Se encarga de validaciones, operaciones complejas y coordinar el acceso a datos.
 */
@Service
@Transactional
public class PersonaService {

    @Autowired
    private PersonaRepository personaRepository;

    /**
     * Obtiene todas las personas registradas.
     */
    public List<Persona> listarTodas() {
        return personaRepository.findAll();
    }

    /**
     * Busca una persona por su ID.
     */
    public Optional<Persona> buscarPorId(Long id) {
        return personaRepository.findById(id);
    }

    /**
     * Busca una persona por su email.
     */
    public Optional<Persona> buscarPorEmail(String email) {
        return personaRepository.findByMail(email);
    }

    /**
     * Crea una nueva persona con validaciones.
     */
    public Persona crear(Persona persona) {
        // Validación: nombre obligatorio
        if (persona.getNom() == null || persona.getNom().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }

        // Validación: email obligatorio
        if (persona.getMail() == null || persona.getMail().trim().isEmpty()) {
            throw new IllegalArgumentException("El email es obligatorio");
        }

        // Validación: email único
        if (personaRepository.existsByMail(persona.getMail())) {
            throw new IllegalArgumentException("El email ya está registrado");
        }

        // Inicializar victorias a 0 si no está definido
        if (persona.getVictories() == 0) {
            persona.setVictories(0);
        }

        // Guardar en base de datos
        return personaRepository.save(persona);
    }

    /**
     * Actualiza una persona existente.
     */
    public Persona actualizar(Long id, Persona personaActualizada) {
        // Buscar la persona existente
        Persona personaExistente = personaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Persona no encontrada con ID: " + id));

        // Validar que el email no esté duplicado (si se cambia)
        if (!personaExistente.getMail().equals(personaActualizada.getMail())) {
            if (personaRepository.existsByMail(personaActualizada.getMail())) {
                throw new IllegalArgumentException("El email ya está registrado");
            }
        }

        // Actualizar campos
        personaExistente.setNom(personaActualizada.getNom());
        personaExistente.setMail(personaActualizada.getMail());
        // No actualizamos victorias aquí, solo cuando gana una partida

        return personaRepository.save(personaExistente);
    }

    /**
     * Elimina una persona por su ID.
     */
    public void eliminar(Long id) {
        if (!personaRepository.existsById(id)) {
            throw new RuntimeException("Persona no encontrada con ID: " + id);
        }
        
        try {
            // 1. Eliminar de la tabla intermedia partida_participantes
            personaRepository.eliminarDePartidas(id);
            
            // 2. Poner a NULL el ganador_id en partidas donde era ganador
            personaRepository.limpiarGanador(id);
            
            // 3. Ahora sí eliminar la persona
            personaRepository.deleteById(id);
        } catch (Exception e) {
            // Si hay error, simplemente intentar eliminar directamente
            // (funcionará si no hay restricciones de FK)
            try {
                personaRepository.deleteById(id);
            } catch (Exception ex) {
                throw new RuntimeException("No se puede eliminar la persona porque está en partidas activas. Elimina primero las partidas.");
            }
        }
    }

    /**
     * Obtiene el ranking de personas ordenadas por victorias (de mayor a menor).
     */
    public List<Persona> obtenerRanking() {
        return personaRepository.findAllOrderByVictoriesDesc();
    }

    /**
     * Suma una victoria a una persona.
     */
    public void sumarVictoria(Long personaId) {
        Persona persona = personaRepository.findById(personaId)
                .orElseThrow(() -> new RuntimeException("Persona no encontrada"));
        persona.sumarVictoria();
        personaRepository.save(persona);
    }
}
