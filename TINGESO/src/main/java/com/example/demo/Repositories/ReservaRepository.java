package com.example.demo.Repositories;

import com.example.demo.Entities.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservaRepository extends JpaRepository<Reserva, Long> {
    @Query("SELECT r FROM Reserva r " +
            "WHERE r.fechaInicio = :fecha " +
            "AND (r.horaInicio < :horaFin AND r.horaFin > :horaInicio)")
    List<Reserva> findReservasQueSeCruzan(@Param("fecha") LocalDate fecha,
                                          @Param("horaInicio") LocalTime horaInicio,
                                          @Param("horaFin") LocalTime horaFin);

    List<Reserva> findByFechaInicioOrderByHoraInicioAsc(LocalDate fecha);

    List<Reserva> findByFechaInicioBetween(LocalDate fechaInicio, LocalDate fechaFin);

    Optional<Reserva> findByFechaInicioAndHoraInicioAndHoraFin(LocalDate fechaInicio, LocalTime horaInicio, LocalTime horaFin);

}
