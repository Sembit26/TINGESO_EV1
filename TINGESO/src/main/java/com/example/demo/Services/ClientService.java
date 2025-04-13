package com.example.demo.Services;

import com.example.demo.Entities.Client;
import com.example.demo.Repositories.ClientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ClientService {

    @Autowired
    private ClientRepository clientRepository;

    //Obtener a todos los clientes
    public List<Client> findAll() {
        return clientRepository.findAll();
    }

    //Buscar cliente por correo electronico
    public Client findByEmail(String email) {
        return clientRepository.findByEmail(email);
    }

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
        Client clienteExistentePorEmail = clientRepository.findByEmail(email);

        // Buscar si ya existe un cliente por RUT
        Client clienteExistentePorRut = clientRepository.findByRut(rut);

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
        return clientRepository.save(nuevoCliente);
    }

    //Obtener un cliente por contraseña y correo (login)
    public Client login(String email, String contrasenia) {
        Optional<Client> clientOpt = Optional.ofNullable(clientRepository.findByEmail(email));
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
                clientRepository.save(client);
                return client;
            }
            throw new RuntimeException("Contrasenia incorrecta");
        }
        throw new RuntimeException("No se encontro el cliente con el email " + email);
    }
}
