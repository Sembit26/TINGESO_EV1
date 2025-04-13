package com.example.demo.Controllers;

import com.example.demo.Entities.Reserva;
import com.example.demo.Services.ReservaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/reservas")
public class ReservaController {

    @Autowired
    private ReservaService reservaService;

    @GetMapping("/getAll")
    public List<Reserva> getAllReservas() {
        return reservaService.findAll();
    }

    @GetMapping("/getById/{id}")
    public Optional<Reserva> getReservaById(@PathVariable Long id) {
        return reservaService.findById(id);
    }

    @PostMapping("/createReserva")
    public Reserva createReserva(@RequestBody Reserva reserva) {
        return reservaService.save(reserva);
    }

    @PutMapping("/updateReservaById/{id}")
    public Reserva updateReserva(@PathVariable Long id, @RequestBody Reserva updatedReserva) {
        return reservaService.update(id, updatedReserva);
    }

    @DeleteMapping("/deleteReservaById/{id}")
    public void deleteReserva(@PathVariable Long id) {
        reservaService.deleteById(id);
    }

    @PostMapping("/crearReserva")
    public ResponseEntity<Reserva> crearReserva(@RequestBody Map<String, Object> body) {
        try {
            Long clientId = Long.valueOf(body.get("clientId").toString());
            int numVueltasTiempoMaximo = Integer.parseInt(body.get("numVueltasTiempoMaximo").toString());
            int numPersonas = Integer.parseInt(body.get("numPersonas").toString());

            @SuppressWarnings("unchecked")
            List<Map<String, String>> personasAcompanantes = (List<Map<String, String>>) body.get("personasAcompanantes");

            // Parsear fechaInicio y horaInicio desde el body
            String fechaInicioStr = body.get("fechaInicio").toString();  // Ej: "2025-04-20"
            String horaInicioStr = body.get("horaInicio").toString();    // Ej: "14:00:00"

            LocalDate fechaInicio = LocalDate.parse(fechaInicioStr);
            LocalTime horaInicio = LocalTime.parse(horaInicioStr);

            // Llamar al servicio con los nuevos parámetros
            Reserva reserva = reservaService.crearReserva(
                    clientId,
                    numVueltasTiempoMaximo,
                    numPersonas,
                    personasAcompanantes,
                    fechaInicio,
                    horaInicio
            );

            return ResponseEntity.ok(reserva);

        } catch (Exception e) {
            e.printStackTrace(); // Opcional para debug
            return ResponseEntity.badRequest().build();
        }
    }



}
