package com.example.demo.Services;

import com.example.demo.Entities.Client;
import com.example.demo.Entities.Reserva;
import com.example.demo.Repositories.ClientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ClientService {

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    public ReservaService reservaService;

    //Obtener a todos los clientes
    public List<Client> findAll() {
        return clientRepository.findAll();
    }

    //Buscar cliente por correo electronico
    public Client findByEmail(String email) {
        return clientRepository.findByEmail(email);
    }

    public Client findByRut(String rut) { return clientRepository.findByRut(rut); }

    //Buscar clientes por Id
    public Optional<Client> findById(Long id) {
        return clientRepository.findById(id);
    }

    //Guardar o crear un cliente
    public Client save(Client client) {
        return clientRepository.save(client);
    }

    //Borrar un cliente por Id (PUEDE QUE NO SE USE)
    public void deleteById(Long id) {
        clientRepository.deleteById(id);
    }

    //Actualizar información de un cliente
    public Client update(Long id, Client updatedClient) {
        return clientRepository.findById(id).map(client -> {
            client.setName(updatedClient.getName());
            client.setEmail(updatedClient.getEmail());
            client.setContrasena(updatedClient.getContrasena());
            client.setRut(updatedClient.getRut());
            client.setBirthday(updatedClient.getBirthday());
            client.setNum_visitas_al_mes(updatedClient.getNum_visitas_al_mes());
            return clientRepository.save(client);
        }).orElse(null);
    }

    //Registrar un cliente en la base de datos
    public Client register(String rut, String nombre, String email, String contrasenia, LocalDate birthday) {
        // Buscar si ya existe un cliente por correo electrónico
        Client clienteExistentePorEmail = findByEmail(email);

        // Buscar si ya existe un cliente por RUT
        Client clienteExistentePorRut = findByRut(rut);

        if (clienteExistentePorEmail != null || clienteExistentePorRut != null) {
            throw new RuntimeException("El cliente con el correo o RUT ya existe.");
        }

        // Si no existe, crear un nuevo cliente
        Client nuevoCliente = new Client();
        nuevoCliente.setRut(rut);
        nuevoCliente.setName(nombre);
        nuevoCliente.setEmail(email);
        nuevoCliente.setContrasena(contrasenia); // Asegúrate de hashear la contraseña
        nuevoCliente.setBirthday(birthday);
        nuevoCliente.setNum_visitas_al_mes(0); // Inicializar a 0
        nuevoCliente.setReservas(new ArrayList<>()); //Inicia con 0 reservas

        // Guardar el nuevo cliente
        return save(nuevoCliente);
    }

    //Obtener un cliente por contraseña y correo (login)
    public Client login(String email, String contrasenia) {
        Optional<Client> clientOpt = Optional.ofNullable(findByEmail(email));
        if (clientOpt.isPresent()) {
            Client client = clientOpt.get();
            if(client.getContrasena().equals(contrasenia)) {
                LocalDate today = LocalDate.now();
                LocalDate lastLogin = client.getLastLoginDate();
                // Si es el primer login o ha cambiado el mes, reiniciar visitas
                if (lastLogin == null || today.getMonthValue() != lastLogin.getMonthValue() || today.getYear() != lastLogin.getYear()) {
                    client.setNum_visitas_al_mes(0);
                }

                // Actualizar la fecha del último login
                client.setLastLoginDate(today);

                // Guardar cambios
                save(client);
                return client;
            }
            throw new RuntimeException("Contrasenia incorrecta");
        }
        throw new RuntimeException("No se encontro el cliente con el email " + email);
    }

    public Reserva generarReserva(Long id, int numVueltasTiempoMaximo, int numPersonas,
                                  List<Map<String, String>> personasAcompanantes,
                                  LocalDate fechaInicio,
                                  LocalTime horaInicio,
                                  List<String> cumpleaneros,
                                  List<String> nombresPersonas) {

        // Buscar el cliente
        Optional<Client> clientOpt = findById(id);
        if (clientOpt.isEmpty()) {
            throw new RuntimeException("Cliente no encontrado");
        }

        Client cliente = clientOpt.get();
        String nombreCliente = cliente.getName();
        int frecuenciaCliente = cliente.getNum_visitas_al_mes();

        // Asegurarse de que el cliente esté en la lista de personas si no está ya
        if (!nombresPersonas.contains(nombreCliente)) {
            nombresPersonas.add(nombreCliente); // No agregar al principio, solo lo añades si no está
        }

        // Crear la reserva usando el servicio
        Reserva reserva = reservaService.crearReserva(
                numVueltasTiempoMaximo,
                numPersonas,
                personasAcompanantes,
                fechaInicio,
                horaInicio,
                frecuenciaCliente,
                nombreCliente,
                cumpleaneros,
                nombresPersonas
        );

        // Asociar la reserva al cliente
        cliente.getReservas().add(reserva);
        cliente.setNum_visitas_al_mes(cliente.getNum_visitas_al_mes() + 1);

        // Guardar el cliente con la nueva reserva
        clientRepository.save(cliente);

        return reserva;
    }



}
