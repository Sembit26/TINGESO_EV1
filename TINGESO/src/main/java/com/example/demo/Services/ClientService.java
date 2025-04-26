package com.example.demo.Services;

import com.example.demo.Entities.Client;
import com.example.demo.Entities.Reserva;
import com.example.demo.Repositories.ClientRepository;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

@Service
public class ClientService {

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    public ReservaService reservaService;

    @Autowired
    private JavaMailSender mailSender;

    // ==================== OPERACIONES BÁSICAS ====================

    public List<Client> findAll() {
        return clientRepository.findAll();
    }

    public Optional<Client> findById(Long id) {
        return clientRepository.findById(id);
    }

    public Client findByEmail(String email) {
        return clientRepository.findByEmail(email);
    }

    public Client findByRut(String rut) {
        return clientRepository.findByRut(rut);
    }

    public Client save(Client client) {
        return clientRepository.save(client);
    }

    public void deleteById(Long id) {
        clientRepository.deleteById(id);
    }

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

    // ==================== REGISTRO Y LOGIN ====================

    public Client register(String rut, String nombre, String email, String contrasenia, LocalDate birthday) {
        if (findByEmail(email) != null || findByRut(rut) != null) {
            throw new RuntimeException("El cliente con el correo o RUT ya existe.");
        }

        Client nuevoCliente = new Client();
        nuevoCliente.setRut(rut);
        nuevoCliente.setName(nombre);
        nuevoCliente.setEmail(email);
        nuevoCliente.setContrasena(contrasenia); // Debería hashearse
        nuevoCliente.setBirthday(birthday);
        nuevoCliente.setNum_visitas_al_mes(0);
        nuevoCliente.setReservas(new ArrayList<>());

        return save(nuevoCliente);
    }

    public Client login(String email, String contrasenia) {
        Client client = findByEmail(email);
        if (client == null) throw new RuntimeException("No se encontró el cliente con el email " + email);

        if (!client.getContrasena().equals(contrasenia)) {
            throw new RuntimeException("Contraseña incorrecta");
        }

        LocalDate today = LocalDate.now();
        LocalDate lastLogin = client.getLastLoginDate();

        if (lastLogin == null || today.getMonthValue() != lastLogin.getMonthValue() || today.getYear() != lastLogin.getYear()) {
            client.setNum_visitas_al_mes(0);
        }

        client.setLastLoginDate(today);
        save(client);
        return client;
    }

    // ==================== GENERACIÓN DE PDF Y CORREO ====================

    public File generarPDFReserva(String resumen) {
        String filePath = "reserva_comprobante.pdf";
        File file = new File(filePath);

        try {
            PdfWriter writer = new PdfWriter(file);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);
            document.add(new Paragraph(resumen));
            document.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return file;
    }

    public void enviarCorreoReservaConPDF(String correo, String cuerpo, File archivoPdf) {
        try {
            MimeMessage mensaje = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mensaje, true);
            helper.setTo(correo);
            helper.setSubject("Resumen de tu Reserva");
            helper.setText(cuerpo, true);
            helper.addAttachment("Resumen_Reserva.pdf", archivoPdf);
            mailSender.send(mensaje);
            System.out.println("Correo enviado a " + correo);
        } catch (MessagingException e) {
            System.err.println("Error al enviar correo a " + correo + ": " + e.getMessage());
        }
    }

    // ==================== GENERAR RESERVA ====================

    public Reserva generarReserva(
            Long id,
            int numVueltasTiempoMaximo,
            int numPersonas,
            List<String> correosCumpleaneros,
            LocalDate fechaInicio,
            LocalTime horaInicio,
            Map<String, String> nombreCorreo
    ) {

        LocalDate hoy = LocalDate.now();

        Optional<Client> clientOpt = findById(id);
        if (clientOpt.isEmpty()) {
            throw new RuntimeException("Cliente no encontrado");
        }

        Client cliente = clientOpt.get();
        String nombreCliente = cliente.getName();
        int frecuenciaCliente = cliente.getNum_visitas_al_mes();
        String correoCliente = cliente.getEmail();

        // Comparar mes y año de la fecha actual con la fechaInicio
        if (hoy.getMonthValue() != fechaInicio.getMonthValue() || hoy.getYear() != fechaInicio.getYear()) {
            frecuenciaCliente = 0;
        }

        // Agregar cliente principal al map
        nombreCorreo.put(nombreCliente, correoCliente);

        if (correosCumpleaneros != null) {
            correosCumpleaneros.removeIf(correoCumple -> {
                Client cumpleanero = findByEmail(correoCumple);
                if (cumpleanero == null || cumpleanero.getBirthday() == null) {
                    return true; // Elimina si no se encuentra o no tiene fecha de cumpleaños
                }
                // Elimina si el mes o el día no coinciden con la fecha de inicio
                return cumpleanero.getBirthday().getMonthValue() != fechaInicio.getMonthValue()
                        || cumpleanero.getBirthday().getDayOfMonth() != fechaInicio.getDayOfMonth();
            });
        }

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

        // Asociar la reserva al cliente
        cliente.getReservas().add(reserva);
        cliente.setNum_visitas_al_mes(frecuenciaCliente + 1);
        save(cliente);

        // Enviar PDF con resumen
        String resumen = reservaService.obtenerInformacionReservaConComprobante(reserva);
        File pdf = generarPDFReserva(resumen);

        // Filtrar y enviar correos válidos
        nombreCorreo.values().stream()
                .filter(correo -> correo != null && !correo.trim().isEmpty())
                .forEach(correo -> enviarCorreoReservaConPDF(correo, "Aquí está tu resumen de reserva", pdf));
        return reserva;
    }
}
