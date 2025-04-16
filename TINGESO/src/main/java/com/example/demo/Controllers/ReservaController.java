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
            // Extraer parámetros básicos del cuerpo de la solicitud
            int numVueltasTiempoMaximo = Integer.parseInt(body.get("numVueltasTiempoMaximo").toString());
            int numPersonas = Integer.parseInt(body.get("numPersonas").toString());

            // Parsear la fecha y hora de inicio
            String fechaInicioStr = body.get("fechaInicio").toString();  // Ej: "2025-04-20"
            String horaInicioStr = body.get("horaInicio").toString();    // Ej: "14:00:00"
            LocalDate fechaInicio = LocalDate.parse(fechaInicioStr);
            LocalTime horaInicio = LocalTime.parse(horaInicioStr);

            // Otros parámetros adicionales
            int frecuenciaCliente = Integer.parseInt(body.get("frecuenciaCliente").toString());
            String nombreCliente = body.get("nombreCliente").toString();

            // Obtener la lista de cumpleaños (es una lista de strings)
            @SuppressWarnings("unchecked")
            List<String> cumpleaneros = (List<String>) body.get("cumpleaneros");

            // Obtener la lista de personas (nombres)
            @SuppressWarnings("unchecked")
            List<String> nombresPersonas = (List<String>) body.get("nombresPersonas");

            // Llamar al servicio para crear la reserva
            Reserva reserva = reservaService.crearReserva(
                    numVueltasTiempoMaximo,
                    numPersonas,
                    nombresPersonas,
                    fechaInicio,
                    horaInicio,
                    frecuenciaCliente,
                    nombreCliente,
                    cumpleaneros
            );

            // Retornar la respuesta con la reserva creada
            return ResponseEntity.ok(reserva);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(null);
        }
    }

    /*
    @GetMapping("/reservas/{id}")
    public String obtenerInformacionReserva(@PathVariable Long id) {
        // Llamar al servicio para obtener la información de la reserva y el comprobante
        return reservaService.obtenerInformacionReservaConComprobante(id);
    }

     */





}
