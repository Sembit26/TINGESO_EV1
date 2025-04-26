package com.example.demo.Controllers;

import com.example.demo.Entities.Empleado;
import com.example.demo.Entities.Reserva;
import com.example.demo.Services.EmpleadoService;
import com.example.demo.Services.ReservaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/empleados")
@CrossOrigin("*")
public class EmpleadoController {

    @Autowired
    private EmpleadoService empleadoService;

    @GetMapping("/getAll")
    public List<Empleado> getAllEmpleados() {
        return empleadoService.getAllEmpleados();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Empleado> getEmpleadoById(@PathVariable Long id) {
        Optional<Empleado> empleado = empleadoService.getEmpleadoById(id);
        return empleado.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/createEmpleado")
    public Empleado createEmpleado(@RequestBody Empleado empleado) {
        return empleadoService.save(empleado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Empleado> updateEmpleado(@PathVariable Long id, @RequestBody Empleado empleadoDetails) {
        Empleado updated = empleadoService.updateEmpleado(id, empleadoDetails);
        if (updated != null) {
            return ResponseEntity.ok(updated);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmpleado(@PathVariable Long id) {
        if (empleadoService.deleteEmpleado(id)) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // Endpoint para registrar un nuevo empleado
    @PostMapping("/register")
    public Empleado registerEmpleado(@RequestBody Map<String, String> request) {
        String rut = request.get("rut");
        String nombre = request.get("nombre");
        String email = request.get("email");
        String contrasena = request.get("contrasena");

        return empleadoService.register(rut, nombre, email, contrasena);
    }

    // Endpoint para login de empleado
    @PostMapping("/login")
    public Empleado loginEmpleado(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String contrasena = request.get("contrasena");

        return empleadoService.login(email, contrasena);
    }

    @PostMapping("/generarReservaEmpleado")
    public ResponseEntity<Reserva> generarReservaPorEmpleado(@RequestBody Map<String, Object> body) {
        try {
            String nombreCliente = body.get("nombreCliente").toString();
            String correoCliente = body.get("correoCliente").toString();
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

            Reserva reserva = empleadoService.generarReserva(
                    nombreCliente,
                    correoCliente,
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
