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
@RequestMapping("/api/reservas")
@CrossOrigin("*")
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
     * Obtener Horarios DISPONIBLES de los proximos seis meses
     */
    @GetMapping("/horariosDisponiblesSeisMeses")
    public ResponseEntity<Map<LocalDate, List<String>>> getHorariosDisponiblesSeisMeses() {
        try {
            LocalDate hoy = LocalDate.now(); // Fecha actual
            Map<LocalDate, List<String>> horarios = reservaService.obtenerHorariosDisponiblesProximosSeisMeses(hoy);
            return ResponseEntity.ok(horarios);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/horariosOcupados")
    public ResponseEntity<Map<LocalDate, List<String>>> getHorariosOcupados() {
        try {
            Map<LocalDate, List<String>> horariosOcupados = reservaService.obtenerTodosLosHorariosOcupados();
            return ResponseEntity.ok(horariosOcupados);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Obtener el ingreso en la pista por cantidad de vueltas o tiempo maximo
     */
    @GetMapping("/ingresosPorVueltas")
    public Map<String, Map<String, Double>> obtenerReporteIngresosPorVueltas(
            @RequestParam("fechaInicio") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam("fechaFin") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {

        if (fechaInicio.isAfter(fechaFin)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La fecha de inicio no puede ser posterior a la fecha de fin.");
        }

        return reservaService.generarReporteIngresosPorVueltas(fechaInicio, fechaFin);
    }

    /**
     * Obtener el ingreso en la pista por cantidad de personas
     */
    @GetMapping("/ingresosPorPersonas")
    public Map<String, Map<String, Double>> obtenerReporteIngresosPorPersonas(
            @RequestParam("fechaInicio") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam("fechaFin") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {

        if (fechaInicio.isAfter(fechaFin)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La fecha de inicio no puede ser posterior a la fecha de fin.");
        }

        return reservaService.generarReporteIngresosPorGrupoDePersonas(fechaInicio, fechaFin);
    }

    @GetMapping("/obtenerReservaPorFechaYHora")
    public Optional<Reserva> obtenerReserva(@RequestParam LocalDate fechaInicio,
                                            @RequestParam LocalTime horaInicio,
                                            @RequestParam LocalTime horaFin) {
        return reservaService.obtenerReservaPorFechaHoraInicioYHoraFin(fechaInicio, horaInicio, horaFin);
    }

    @GetMapping("/getInfoReserva/{id}")
    public ResponseEntity<String> obtenerInformacionReserva(@PathVariable Long id) {
        Optional<Reserva> reservaOptional = reservaService.findById(id);

        if (reservaOptional.isPresent()) {
            Reserva reserva = reservaOptional.get();
            String informacionReserva = reservaService.obtenerInformacionReservaConComprobante(reserva);
            return ResponseEntity.ok(informacionReserva);
        } else {
            return ResponseEntity.notFound().build();  // Retorna 404 si no se encuentra la reserva
        }
    }

}
