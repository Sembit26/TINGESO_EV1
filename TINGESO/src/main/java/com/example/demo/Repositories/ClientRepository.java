package com.example.demo.Repositories;

import com.example.demo.Entities.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {

    // Ejemplo de método útil si quieres buscar por rut o email
    Client findByRut(String rut);
    Client findByEmail(String email);
    Client findById(long id);
}
