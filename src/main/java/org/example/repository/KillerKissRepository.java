package org.example.repository;

import org.example.KillerKiss;
import org.example.Persona;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad KillerKiss (partidas).
 * Spring Data JPA genera automáticamente la implementación de esta interfaz.
 * 
 * Métodos automáticos disponibles:
 * - save(KillerKiss k) - Guarda o actualiza una partida
 * - findAll() - Obtiene todas las partidas
 * - findById(Long id) - Busca partida por ID
 * - delete(KillerKiss k) - Elimina una partida
 * - count() - Cuenta total de partidas
 */
@Repository
public interface KillerKissRepository extends JpaRepository<KillerKiss, Long> {
    
    /**
     * Busca partidas activas (estat = true).
     */
    List<KillerKiss> findByEstatTrue();
    
    /**
     * Busca partidas finalizadas (estat = false).
     */
    List<KillerKiss> findByEstatFalse();
    
    /**
     * Cuenta partidas por estado.
     */
    long countByEstat(boolean estat);
    
    /**
     * Busca partidas por nombre.
     */
    Optional<KillerKiss> findByNom(String nom);
    
    /**
     * Busca si existe una partida activa con ese nombre.
     */
    boolean existsByNomAndEstatTrue(String nom);
    
    /**
     * Busca partidas ganadas por una persona específica.
     */
    List<KillerKiss> findByGanador(Persona ganador);
}
