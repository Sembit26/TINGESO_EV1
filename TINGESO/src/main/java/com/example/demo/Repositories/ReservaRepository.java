package com.example.demo.Repositories;

import com.example.demo.Entities.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReservaRepository extends JpaRepository<Reserva, Long> {

    // Ejemplo: buscar reservas por cliente
    List<Reserva> findByClienteId(Long clienteId);
}
