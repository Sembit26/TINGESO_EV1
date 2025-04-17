package com.example.demo.Controllers;

import com.example.demo.Entities.Reserva;
import com.example.demo.Services.ReservaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

/**
 * Controlador REST para la gestión de reservas.
 */
@RestController
@RequestMapping("/reservas")
public class ReservaController {

    @Autowired
    private ReservaService reservaService;

    // ==================== CRUD BÁSICO ====================

    /**
     * Obtener todas las reservas.
     */
    @GetMapping("/getAll")
    public List<Reserva> getAllReservas() {
        return reservaService.findAll();
    }

    /**
     * Obtener una reserva por su ID.
     */
    @GetMapping("/getById/{id}")
    public Optional<Reserva> getReservaById(@PathVariable Long id) {
        return reservaService.findById(id);
    }

    /**
     * Crear una nueva reserva.
     */
    @PostMapping("/createReserva")
    public Reserva createReserva(@RequestBody Reserva reserva) {
        return reservaService.save(reserva);
    }

    /**
     * Actualizar una reserva existente por ID.
     */
    @PutMapping("/updateReservaById/{id}")
    public Reserva updateReserva(@PathVariable Long id, @RequestBody Reserva updatedReserva) {
        return reservaService.update(id, updatedReserva);
    }

    /**
     * Eliminar una reserva por ID.
     */
    @DeleteMapping("/deleteReservaById/{id}")
    public void deleteReserva(@PathVariable Long id) {
        reservaService.deleteById(id);
    }

    // ==================== FUNCIÓN PERSONALIZADA ====================

    /**
     * Crear una reserva de forma dinámica desde un mapa JSON.
     */
    @PostMapping("/crearReserva")
    public ResponseEntity<Reserva> crearReserva(@RequestBody Map<String, Object> body) {
        try {
            // Parámetros básicos
            int numVueltasTiempoMaximo = Integer.parseInt(body.get("numVueltasTiempoMaximo").toString());
            int numPersonas = Integer.parseInt(body.get("numPersonas").toString());

            // Fecha y hora de inicio
            LocalDate fechaInicio = LocalDate.parse(body.get("fechaInicio").toString());
            LocalTime horaInicio = LocalTime.parse(body.get("horaInicio").toString());

            // Datos del cliente
            int frecuenciaCliente = Integer.parseInt(body.get("frecuenciaCliente").toString());
            String nombreCliente = body.get("nombreCliente").toString();
            String correoCliente = body.get("correoCliente").toString();

            // Correos de cumpleañeros
            @SuppressWarnings("unchecked")
            List<String> correosCumpleaneros = (List<String>) body.get("correosCumpleaneros");

            // Mapa de nombres y correos
            @SuppressWarnings("unchecked")
            Map<String, String> nombreCorreo = (Map<String, String>) body.get("nombreCorreo");

            // Crear y retornar la reserva
            Reserva reserva = reservaService.crearReserva(
                    numVueltasTiempoMaximo,
                    numPersonas,
                    correosCumpleaneros,
                    fechaInicio,
                    horaInicio,
                    frecuenciaCliente,
                    nombreCliente,
                    correoCliente,
                    nombreCorreo
            );

            return ResponseEntity.ok(reserva);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(null);
        }
    }

    /**
     * Obtener Horario Disponibles en la semana
     */
    @GetMapping("/horariosDisponiblesSemana")
    public ResponseEntity<Map<LocalDate, List<String>>> getHorariosDisponiblesSemana() {
        try {
            LocalDate inicioSemana = reservaService.calcularInicioSemana(LocalDate.now()); // Calcula el inicio de la semana a partir de la fecha actual
            Map<LocalDate, List<String>> horarios = reservaService.obtenerHorariosDisponiblesSemana(inicioSemana);
            return ResponseEntity.ok(horarios);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Obtener Horarios que tienen reserva en la semana
     */
    @GetMapping("/horariosOcupadosSemana")
    public ResponseEntity<Map<LocalDate, List<String>>> getHorariosOcupadosSemana() {
        try {
            LocalDate inicioSemana = reservaService.calcularInicioSemana(LocalDate.now()); // Calcula el inicio de la semana a partir de la fecha actual
            Map<LocalDate, List<String>> horariosOcupados = reservaService.obtenerHorariosOcupadosSemana(inicioSemana);
            return ResponseEntity.ok(horariosOcupados);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/ingresos-vueltas")
    public Map<String, Map<String, Double>> obtenerReporteIngresosPorVueltas(
            @RequestParam("fechaInicio") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam("fechaFin") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {

        if (fechaInicio.isAfter(fechaFin)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La fecha de inicio no puede ser posterior a la fecha de fin.");
        }

        return reservaService.generarReporteIngresosPorVueltas(fechaInicio, fechaFin);
    }


}
