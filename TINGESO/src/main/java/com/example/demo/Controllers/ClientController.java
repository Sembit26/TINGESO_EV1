package com.example.demo.Controllers;
import com.example.demo.Entities.Reserva;

import com.example.demo.Entities.Client;
import com.example.demo.Services.ClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/clients")
public class ClientController {

    @Autowired
    private ClientService clientService;

    // Obtener todos los clientes
    @GetMapping("/getAll")
    public List<Client> getAllClients() {
        return clientService.findAll();
    }

    // Obtener cliente por ID
    @GetMapping("/getId/{id}")
    public ResponseEntity<Client> getClientById(@PathVariable Long id) {
        Optional<Client> client = clientService.findById(id);
        return client.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Obtener cliente por email
    @GetMapping("/getByEmail/{email}")
    public ResponseEntity<Client> getClientByEmail(@PathVariable String email) {
        Client client = clientService.findByEmail(email);
        return client != null ? ResponseEntity.ok(client) : ResponseEntity.notFound().build();
    }

    // Crear nuevo cliente directamente
    @PostMapping("/creatClient")
    public ResponseEntity<Client> createClient(@RequestBody Client client) {
        return ResponseEntity.ok(clientService.save(client));
    }

    // Actualizar cliente existente
    @PutMapping("/UpdateClient/{id}")
    public ResponseEntity<Client> updateClient(@PathVariable Long id, @RequestBody Client updatedClient) {
        Client updated = clientService.update(id, updatedClient);
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }

    // Eliminar cliente por ID
    @DeleteMapping("/deleteClientById/{id}")
    public ResponseEntity<Void> deleteClient(@PathVariable Long id) {
        clientService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // Registrar nuevo cliente
    @PostMapping("/register")
    public ResponseEntity<Client> registerClient(@RequestBody Client client) {
        Client registeredClient = clientService.register(
                client.getRut(),
                client.getName(),
                client.getEmail(),
                client.getContrasena(),
                client.getBirthday()
        );
        return registeredClient != null ? ResponseEntity.ok(registeredClient) : ResponseEntity.badRequest().build();
    }

    //login para el cliente
    @PostMapping("/login")
    public ResponseEntity<Client> loginClient(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String contrasenia = body.get("contrasenia");
        try {
            Client client = clientService.login(email, contrasenia);
            return ResponseEntity.ok(client);
        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body(null); // Unauthorized
        }
    }


    @PostMapping("/generarReserva/{id}")
    public ResponseEntity<Reserva> generarReserva(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        try {
            int numVueltasTiempoMaximo = Integer.parseInt(body.get("numVueltasTiempoMaximo").toString());
            int numPersonas = Integer.parseInt(body.get("numPersonas").toString());
            LocalDate fechaInicio = LocalDate.parse(body.get("fechaInicio").toString());
            LocalTime horaInicio = LocalTime.parse(body.get("horaInicio").toString());

            // Convertir las listas del body
            List<String> cumpleaneros = (List<String>) body.get("cumpleaneros");
            List<String> nombres = (List<String>) body.get("nombres");
            List<String> correos = (List<String>) body.get("correos"); // Añadir los correos al body

            // Llamar al service
            Reserva reserva = clientService.generarReserva(
                    id,
                    numVueltasTiempoMaximo,
                    numPersonas,
                    nombres,             // nombresPersonas
                    fechaInicio,
                    horaInicio,
                    cumpleaneros,
                    correos             // Pasar la lista de correos
            );

            return ResponseEntity.ok(reserva);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }








}
