package com.example.demo.Repositories;

import com.example.demo.Entities.Empleado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmpleadoRepository extends JpaRepository<Empleado, Long> {

    // Ejemplo de método útil si quieres buscar por rut o email
    Empleado findByRut(String rur);
    Empleado findById(long id);
    Empleado findByEmail(String email);
}
