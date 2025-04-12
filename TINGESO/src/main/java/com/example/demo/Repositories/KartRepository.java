package com.example.demo.Repositories;

import com.example.demo.Entities.Kart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KartRepository extends JpaRepository<Kart, Long> {

    // Ejemplo: buscar todos los karts disponibles
    List<Kart> findByDisponibleTrue();
}
