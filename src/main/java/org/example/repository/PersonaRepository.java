package org.example.repository;

import org.example.Persona;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad Persona.
 * Spring Data JPA genera automáticamente la implementación de esta interfaz.
 * 
 * Métodos automáticos disponibles:
 * - save(Persona p) - Guarda o actualiza una persona
 * - findAll() - Obtiene todas las personas
 * - findById(Long id) - Busca persona por ID
 * - delete(Persona p) - Elimina una persona
 * - count() - Cuenta total de personas
 */
@Repository
public interface PersonaRepository extends JpaRepository<Persona, Long> {
    
    /**
     * Busca una persona por su email.
     * Spring genera automáticamente la consulta SQL.
     */
    Optional<Persona> findByMail(String mail);
    
    /**
     * Verifica si existe una persona con ese email.
     */
    boolean existsByMail(String mail);

    /**
     * Obtiene todas las personas ordenadas por victorias (de mayor a menor).
     */
    @Query("SELECT p FROM Persona p ORDER BY p.victories DESC")
    List<Persona> findAllOrderByVictoriesDesc();
    
    /**
     * Elimina las referencias de una persona en la tabla intermedia partida_participantes.
     */
    @Modifying
    @Query(value = "DELETE FROM partida_participantes WHERE persona_id = :personaId", nativeQuery = true)
    void eliminarDePartidas(@Param("personaId") Long personaId);
    
    /**
     * Pone a NULL el ganador en partidas donde esta persona era ganadora.
     */
    @Modifying
    @Query(value = "UPDATE partidas SET ganador_id = NULL WHERE ganador_id = :personaId", nativeQuery = true)
    void limpiarGanador(@Param("personaId") Long personaId);
}
