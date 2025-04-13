package com.example.demo.Repositories;

import com.example.demo.Entities.Kart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface KartRepository extends JpaRepository<Kart, Long> {
    @Query("""
    SELECT k FROM Kart k WHERE k NOT IN (
        SELECT k2 FROM Reserva r
        JOIN r.kartsAsignados k2
        WHERE r.fechaInicio = :fecha
        AND r.horaInicio < :horaFin
        AND r.horaFin > :horaInicio
    )
""")
    List<Kart> findKartsDisponibles(@Param("fecha") LocalDate fecha,
                                    @Param("horaInicio") LocalTime horaInicio,
                                    @Param("horaFin") LocalTime horaFin);
}

