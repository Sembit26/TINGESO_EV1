package com.example.demo.Controllers;

import com.example.demo.Entities.Client;
import com.example.demo.Entities.Reserva;
import com.example.demo.Services.ClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

@RestController
@RequestMapping("/api/clients")
@CrossOrigin("*")
public class ClientController {

    @Autowired
    private ClientService clientService;

    // ===================== CONSULTAS =====================

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

    // ===================== CREACIÓN =====================

    // Crear nuevo cliente directamente
    @PostMapping("/creatClient")
    public ResponseEntity<Client> createClient(@RequestBody Client client) {
        return ResponseEntity.ok(clientService.save(client));
    }

    // Registrar nuevo cliente (registro completo)
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

    // ===================== AUTENTICACIÓN =====================

    // Login para cliente
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

    // ===================== ACTUALIZACIÓN =====================

    // Actualizar cliente existente
    @PutMapping("/UpdateClient/{id}")
    public ResponseEntity<Client> updateClient(@PathVariable Long id, @RequestBody Client updatedClient) {
        Client updated = clientService.update(id, updatedClient);
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }

    // ===================== ELIMINACIÓN =====================

    // Eliminar cliente por ID
    @DeleteMapping("/deleteClientById/{id}")
    public ResponseEntity<Void> deleteClient(@PathVariable Long id) {
        clientService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // ===================== RESERVAS =====================

    // Generar una nueva reserva para un cliente
    @PostMapping("/generarReserva/{id}")
    public ResponseEntity<Reserva> generarReserva(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        try {
            int numVueltasTiempoMaximo = Integer.parseInt(body.get("numVueltasTiempoMaximo").toString());
            int numPersonas = Integer.parseInt(body.get("numPersonas").toString());
            LocalDate fechaInicio = LocalDate.parse(body.get("fechaInicio").toString());
            LocalTime horaInicio = LocalTime.parse(body.get("horaInicio").toString());

            List<String> cumpleaneros = (List<String>) body.get("cumpleaneros");
            List<String> nombres = (List<String>) body.get("nombres");
            List<String> correos = (List<String>) body.get("correos");

            Map<String, String> nombreCorreo = new HashMap<>();
            if (nombres != null && correos != null && nombres.size() == correos.size()) {
                for (int i = 0; i < nombres.size(); i++) {
                    nombreCorreo.put(nombres.get(i), correos.get(i));
                }
            } else {
                throw new IllegalArgumentException("Listas de nombres y correos no coinciden o son nulas");
            }

            Reserva reserva = clientService.generarReserva(
                    id,
                    numVueltasTiempoMaximo,
                    numPersonas,
                    cumpleaneros,
                    fechaInicio,
                    horaInicio,
                    nombreCorreo
            );

            return ResponseEntity.ok(reserva);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }
}
