package com.example.demo.Services;

import com.example.demo.Entities.Client;
import com.example.demo.Entities.Kart;
import com.example.demo.Entities.Reserva;
import com.example.demo.Repositories.ClientRepository;
import com.example.demo.Repositories.KartRepository;
import com.example.demo.Repositories.ReservaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ReservaService {

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private KartService kartService;

    @Autowired
    private ClientService clientService;

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
            reserva.setCliente(updatedReserva.getCliente());
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


    //Asigna kart por disponibilidad
    public void asignarKartsDisponibles(List<Kart> kartsDisponibles, Reserva reserva) {
        if (kartsDisponibles.size() < reserva.getNum_personas()) {
            throw new RuntimeException("No hay suficientes karts disponibles para el número de personas");
        }

        List<Kart> kartsAsignados = new ArrayList<>();
        for (int i = 0; i < reserva.getNum_personas(); i++) {
            Kart kart = kartsDisponibles.get(i);
            kart.setDisponible(false);
            kartsAsignados.add(kart);
        }

        for (Kart kart : kartsAsignados) {
            kartService.save(kart);
        }

        reserva.setKartsAsignados(kartsAsignados);
    }


    public Reserva crearReserva(Long client_id, int numVueltasTiempoMaximo, int numPersonas, List<Map<String, String>> personasAcompanantes) {

        List<String> personasReserva = new ArrayList<>();
        for (Map<String, String> persona : personasAcompanantes) {
            String nombre = persona.get("nombre");
            String correo = persona.get("correo");
            personasReserva.add(nombre + "," + correo);
        }

        // Obtener al cliente que generó la reserva
        Optional<Client> cliente = clientService.findById(client_id);
        if (cliente.isEmpty()) {
            throw new RuntimeException("Cliente no encontrado");
        }

        Client clienteP = cliente.get();
        clienteP.setNum_visitas_al_mes(clienteP.getNum_visitas_al_mes() + 1);
        clientService.save(clienteP);

        // Crear nueva reserva
        Reserva reserva = new Reserva();
        reserva.setCliente(clienteP);
        reserva.setNum_vueltas_tiempo_maximo(numVueltasTiempoMaximo);
        reserva.setNum_personas(numPersonas);
        reserva.setPersonasReserva(personasReserva);
        reserva.setFechaHora(java.time.LocalDateTime.now());

        // Asignar precio y duración a la misma reserva
        asignarPrecioRegular_DuracionTotal(reserva);

        // Obtener karts disponibles y asignarlos a la reserva
        List<Kart> kartsDisponibles = kartService.finKartsByDisponibilidad();
        asignarKartsDisponibles(kartsDisponibles, reserva);

        // Guardar la reserva
        return reservaRepository.save(reserva);
    }

}
