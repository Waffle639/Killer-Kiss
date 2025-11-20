package org.example.repository;

import org.example.entity.EmailCounter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface EmailCounterRepository extends JpaRepository<EmailCounter, Long> {
    Optional<EmailCounter> findByFecha(LocalDate fecha);
}
