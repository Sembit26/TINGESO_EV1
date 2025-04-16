package com.example.demo.Services;

import com.example.demo.Entities.Client;
import com.example.demo.Entities.Reserva;
import com.example.demo.Repositories.ClientRepository;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.element.Paragraph;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import com.itextpdf.layout.Document;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ClientService {

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    public ReservaService reservaService;

    @Autowired
    private JavaMailSender mailSender;

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

    public void enviarCorreoReservaConPDF(String correo, String cuerpo, File archivoPdf) {
        try {
            // Crear un MimeMessage para enviar un correo con adjunto
            MimeMessage mensaje = mailSender.createMimeMessage();

            // Usar MimeMessageHelper para facilitar la creación del mensaje
            MimeMessageHelper helper = new MimeMessageHelper(mensaje, true); // true habilita el soporte para adjuntos

            // Configurar el destinatario, asunto y cuerpo del mensaje
            helper.setTo(correo);
            helper.setSubject("Resumen de tu Reserva");
            helper.setText(cuerpo, true); // true indica que el cuerpo puede contener HTML

            // Adjuntar el archivo PDF
            helper.addAttachment("Resumen_Reserva.pdf", archivoPdf);

            // Enviar el correo
            mailSender.send(mensaje);

            System.out.println("Correo enviado a " + correo);
        } catch (MessagingException e) {
            System.err.println("Error al enviar correo a " + correo + ": " + e.getMessage());
        }
    }

    public File generarPDFReserva(String resumen) {
        // Crear un archivo PDF temporal
        String filePath = "reserva_comprobante.pdf";
        File file = new File(filePath);

        try {
            // Crear el PdfWriter, que define el archivo de salida
            PdfWriter writer = new PdfWriter(file);

            // Crear un PdfDocument, que usará el PdfWriter
            PdfDocument pdf = new PdfDocument(writer);

            // Crear un documento de iText, que contendrá los elementos (como párrafos)
            Document document = new Document(pdf);

            // Añadir el resumen de la reserva como un párrafo al PDF
            document.add(new Paragraph(resumen));

            // Cerrar el documento
            document.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        // Devolver el archivo generado
        return file;
    }


    public Reserva generarReserva(Long id,
                                  int numVueltasTiempoMaximo,
                                  int numPersonas,
                                  List<String> nombresPersonas,
                                  LocalDate fechaInicio,
                                  LocalTime horaInicio,
                                  List<String> cumpleaneros,
                                  List<String> correos) {

        // Buscar el cliente
        Optional<Client> clientOpt = findById(id);
        if (clientOpt.isEmpty()) {
            throw new RuntimeException("Cliente no encontrado");
        }

        Client cliente = clientOpt.get();
        String nombreCliente = cliente.getName();
        int frecuenciaCliente = cliente.getNum_visitas_al_mes();

        if (!nombresPersonas.contains(nombreCliente)) {
            nombresPersonas.add(0, nombreCliente);
        }
        if(!correos.contains(cliente.getEmail())) {
            correos.add(0, cliente.getEmail());
        }

        // Crear la reserva usando el servicio
        Reserva reserva = reservaService.crearReserva(
                numVueltasTiempoMaximo,
                numPersonas,
                nombresPersonas,
                fechaInicio,
                horaInicio,
                frecuenciaCliente,
                nombreCliente,
                cumpleaneros);

        // Asociar la reserva al cliente
        cliente.getReservas().add(reserva);
        cliente.setNum_visitas_al_mes(cliente.getNum_visitas_al_mes() + 1);

        // Guardar el cliente con la nueva reserva
        clientRepository.save(cliente);

        String resumen = reservaService.obtenerInformacionReservaConComprobante(reserva);
        // Crear un archivo PDF con ese resumen
        File pdf = generarPDFReserva(resumen);

        // Enviar el correo con el archivo PDF a cada correo en la lista de correos
        for (String correo : correos) {
            enviarCorreoReservaConPDF(correo, "Aquí está tu resumen de reserva", pdf);
        }

        return reserva;
    }





}
