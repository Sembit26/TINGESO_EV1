package com.example.demo.Services;

import com.example.demo.Entities.Comprobante;
import com.example.demo.Entities.Kart;
import com.example.demo.Entities.Reserva;
import com.example.demo.Repositories.KartRepository;
import com.example.demo.Repositories.ReservaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
public class ReservaService {

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private ComprobanteService comprobanteService;

    @Autowired
    private KartService kartService;

    public List<Reserva> findAll() {
        return reservaRepository.findAll();
    }

    public Optional<Reserva> findById(Long id) {
        return reservaRepository.findById(id);
    }

    //Crear reserva
    public Reserva save(Reserva reserva) {

        return reservaRepository.save(reserva);
    }

    public void deleteById(Long id) {
        reservaRepository.deleteById(id);
    }

    public Reserva update(Long id, Reserva updatedReserva) {
        return reservaRepository.findById(id).map(reserva -> {
            reserva.setKartsAsignados(updatedReserva.getKartsAsignados());
            reserva.setNum_vueltas_tiempo_maximo(updatedReserva.getNum_vueltas_tiempo_maximo());
            reserva.setNum_personas(updatedReserva.getNum_personas());
            reserva.setPrecio_regular(updatedReserva.getPrecio_regular());
            reserva.setDuracion_total(updatedReserva.getDuracion_total());
            reserva.setFechaHora(updatedReserva.getFechaHora());
            return reservaRepository.save(reserva);
        }).orElse(null);
    }

    //Asigna el precio dependiendo de la cantidad de vueltas o tiempo maximo
    public void asignarPrecioRegular_DuracionTotal(Reserva reserva) {
        if (reserva.getNum_vueltas_tiempo_maximo() == 10) {
            reserva.setPrecio_regular(15000);
            reserva.setDuracion_total(30);
        } else if (reserva.getNum_vueltas_tiempo_maximo() == 15) {
            reserva.setPrecio_regular(20000);
            reserva.setDuracion_total(35);
        } else if (reserva.getNum_vueltas_tiempo_maximo() == 20) {
            reserva.setPrecio_regular(25000);
            reserva.setDuracion_total(40);
        }
    }

    public boolean esReservaPosible(LocalDate fecha, LocalTime horaInicio, LocalTime horaFin) {
        List<Reserva> reservasQueSeCruzan = reservaRepository.findReservasQueSeCruzan(fecha, horaInicio, horaFin);
        return reservasQueSeCruzan.isEmpty(); // Si no hay reservas cruzadas, es posible reservar
    }


    public Reserva crearReserva(int numVueltasTiempoMaximo,
                                int numPersonas,
                                List<String> nombresPersonas,
                                LocalDate fechaInicio,
                                LocalTime horaInicio,
                                int frecuenciaCliente,
                                String nombreCliente,
                                List<String> cumpleaneros) {

        // Crear nueva reserva
        Reserva reserva = new Reserva();
        reserva.setNum_vueltas_tiempo_maximo(numVueltasTiempoMaximo);
        reserva.setNum_personas(numPersonas);
        reserva.setFechaHora(LocalDateTime.now());

        reserva.setFechaInicio(fechaInicio);
        reserva.setHoraInicio(horaInicio);

        // Asignar precio y duración a la misma reserva
        asignarPrecioRegular_DuracionTotal(reserva);

        LocalTime horaFin = horaInicio.plusMinutes(reserva.getDuracion_total());
        reserva.setHoraFin(horaFin);

        if (!esReservaPosible(fechaInicio, horaInicio, horaFin)) {
            throw new RuntimeException("Ya existe una reserva en ese horario.");
        }

        List<Kart> kartsDisponibles = kartService.findAll();
        reserva.setKartsAsignados(kartsDisponibles.subList(0, numPersonas));

        Comprobante comprobante = comprobanteService.crearComprobante(reserva.getPrecio_regular(),
                numPersonas, frecuenciaCliente, nombreCliente, cumpleaneros, nombresPersonas);
        reserva.setComprobante(comprobante);

        // Guardar la reserva
        return save(reserva);
    }

    public String obtenerInformacionReservaConComprobante(Reserva reserva) {

        // Obtener el comprobante de la reserva
        Comprobante comprobante = reserva.getComprobante();

        // Crear el StringBuilder para la información
        StringBuilder informacionReserva = new StringBuilder();
        informacionReserva.append("========= INFORMACIÓN DE LA RESERVA =========\n");
        informacionReserva.append("Código de la reserva: ").append(reserva.getId()).append("\n");
        informacionReserva.append("Fecha y hora de la reserva: ").append(reserva.getFechaHora()).append("\n");
        informacionReserva.append("Fecha de inicio: ").append(reserva.getFechaInicio()).append("\n");
        informacionReserva.append("Hora de inicio: ").append(reserva.getHoraInicio()).append("\n");
        informacionReserva.append("Hora de fin: ").append(reserva.getHoraFin()).append("\n");
        informacionReserva.append("Número de vueltas o tiempo máximo reservado: ").append(reserva.getNum_vueltas_tiempo_maximo()).append("\n");
        informacionReserva.append("Cantidad de personas incluidas: ").append(reserva.getNum_personas()).append("\n");
        informacionReserva.append("Nombre de la persona que hizo la reserva: ").append(comprobante.getDetallePagoPorPersona().get(0).split("\\|")[0]).append("\n");

        // Obtener y añadir la información del comprobante
        informacionReserva.append("\n").append(comprobanteService.formatearComprobante(comprobante));

        // Devolver la información formateada
        return informacionReserva.toString();
    }



}
