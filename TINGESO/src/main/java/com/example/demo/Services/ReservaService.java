package com.example.demo.Services;

import com.example.demo.Entities.Comprobante;
import com.example.demo.Entities.Kart;
import com.example.demo.Entities.Reserva;
import com.example.demo.Repositories.KartRepository;
import com.example.demo.Repositories.ReservaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ReservaService {

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private KartRepository kartRepository;
    @Autowired
    private ComprobanteService comprobanteService;

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

    //Obtiene karts disponibles en una fecha, hora inicio y hora fin
    public List<Kart> obtenerKartsDisponibles(LocalDate fecha, LocalTime horaInicio, LocalTime horaFin) {
        return kartRepository.findKartsDisponibles(fecha, horaInicio, horaFin);
    }

    public Reserva crearReserva(int numVueltasTiempoMaximo, int numPersonas,
                                List<Map<String, String>> personasAcompanantes,
                                LocalDate fechaInicio,
                                LocalTime horaInicio,
                                int frecuenciaCliente,
                                String nombreCliente,
                                List<String> cumpleaneros,
                                List<String> nombres) {

        List<String> personasReserva = new ArrayList<>(); //Nombre y correo de las personas acompañantes
        for (Map<String, String> persona : personasAcompanantes) {
            String nombre = persona.get("nombre");
            String correo = persona.get("correo");
            personasReserva.add(nombre + "," + correo);
        }

        /* Obtener al cliente que generó la reserva
        Optional<Client> cliente = clientService.findById(client_id);
        if (cliente.isEmpty()) {
            throw new RuntimeException("Cliente no encontrado");
        }

        Client clienteP = cliente.get();
        clienteP.setNum_visitas_al_mes(clienteP.getNum_visitas_al_mes() + 1);
        clientService.save(clienteP);
        */

        // Crear nueva reserva
        Reserva reserva = new Reserva();
        reserva.setNum_vueltas_tiempo_maximo(numVueltasTiempoMaximo);
        reserva.setNum_personas(numPersonas);
        reserva.setPersonasReserva(personasReserva);
        reserva.setFechaHora(java.time.LocalDateTime.now());

        reserva.setFechaInicio(fechaInicio);
        reserva.setHoraInicio(horaInicio);

        // Asignar precio y duración a la misma reserva
        asignarPrecioRegular_DuracionTotal(reserva);

        LocalTime horaFin = horaInicio.plusMinutes(reserva.getDuracion_total());
        reserva.setHoraFin(horaFin);

        List<Kart> kartsDisponibles = obtenerKartsDisponibles(fechaInicio, horaInicio, horaFin);
        if(kartsDisponibles.size() < numPersonas){
            throw new RuntimeException("No hay suficientes karts disponibles en ese horario");
        }
        reserva.setKartsAsignados(kartsDisponibles.subList(0, numPersonas));

        Comprobante comprobante = comprobanteService.crearComprobante(reserva.getPrecio_regular(),
                numPersonas, frecuenciaCliente, nombreCliente, cumpleaneros, nombres);
        reserva.setComprobante(comprobante);
        // Guardar la reserva
        return reservaRepository.save(reserva);
    }

}
