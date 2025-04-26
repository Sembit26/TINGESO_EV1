package com.example.demo.Services;

import com.example.demo.Entities.Client;
import com.example.demo.Entities.Empleado;
import com.example.demo.Entities.Reserva;
import com.example.demo.Repositories.EmpleadoRepository;
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
import java.util.List;
import java.util.Map;
import java.util.Optional;



@Service
public class EmpleadoService {

    @Autowired
    private EmpleadoRepository empleadoRepository;

    @Autowired
    public ReservaService reservaService;

    @Autowired
    public ClientService clientService;

    @Autowired
    private JavaMailSender mailSender;


    public List<Empleado> getAllEmpleados() {
        return empleadoRepository.findAll();
    }

    public Optional<Empleado> getEmpleadoById(Long id) {
        return empleadoRepository.findById(id);
    }

    public Empleado save(Empleado empleado) {
        return empleadoRepository.save(empleado);
    }

    public Empleado updateEmpleado(Long id, Empleado empleadoDetails) {
        Optional<Empleado> optionalEmpleado = empleadoRepository.findById(id);
        if (optionalEmpleado.isPresent()) {
            Empleado empleado = optionalEmpleado.get();
            empleado.setRut(empleadoDetails.getRut());
            empleado.setName(empleadoDetails.getName());
            empleado.setEmail(empleadoDetails.getEmail());
            empleado.setContrasena(empleadoDetails.getContrasena());
            return empleadoRepository.save(empleado);
        } else {
            return null;
        }
    }

    public boolean deleteEmpleado(Long id) {
        if (empleadoRepository.existsById(id)) {
            empleadoRepository.deleteById(id);
            return true;
        } else {
            return false;
        }
    }

    public Empleado findByEmail(String email) {
        return empleadoRepository.findByEmail(email);
    }

    public Empleado findByRut(String rut) {
        return empleadoRepository.findByRut(rut);
    }

    public Empleado register(String rut, String nombre, String email, String contrasena) {
        if (findByEmail(email) != null || findByRut(rut) != null) {
            throw new RuntimeException("El empleado con el correo o RUT ya existe.");
        }

        Empleado nuevoEmpleado = new Empleado();
        nuevoEmpleado.setRut(rut);
        nuevoEmpleado.setName(nombre);
        nuevoEmpleado.setEmail(email);
        nuevoEmpleado.setContrasena(contrasena); // Aquí deberías usar hashing si manejas seguridad

        return save(nuevoEmpleado);
    }

    public Empleado login(String email, String contrasena) {
        Empleado empleado = findByEmail(email);
        if (empleado == null) {
            throw new RuntimeException("No se encontró el empleado con el email " + email);
        }

        if (!empleado.getContrasena().equals(contrasena)) {
            throw new RuntimeException("Contraseña incorrecta");
        }

        return empleado;
    }

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

    public Reserva generarReserva(
            String nombreCliente,
            String correoCliente,
            int numVueltasTiempoMaximo,
            int numPersonas,
            List<String> correosCumpleaneros,
            LocalDate fechaInicio,
            LocalTime horaInicio,
            Map<String, String> nombreCorreo
    ) {
        // Frecuencia siempre en 0 porque es una reserva generada por administrador
        int frecuenciaCliente = 0;

        // Agregar cliente principal al mapa
        nombreCorreo.put(nombreCliente, correoCliente);

        // Validar correos de cumpleañeros si existen
        if (correosCumpleaneros != null) {
            correosCumpleaneros.removeIf(correoCumple -> {
                Client cumpleanero = clientService.findByEmail(correoCumple);
                if (cumpleanero == null || cumpleanero.getBirthday() == null) {
                    return true; // eliminar si no hay info
                }
                // Eliminar si su cumpleaños no coincide con la fecha
                return cumpleanero.getBirthday().getMonthValue() != fechaInicio.getMonthValue()
                        || cumpleanero.getBirthday().getDayOfMonth() != fechaInicio.getDayOfMonth();
            });
        }

        // Crear la reserva
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

        // Generar el resumen y PDF
        String resumen = reservaService.obtenerInformacionReservaConComprobante(reserva);
        File pdf = generarPDFReserva(resumen);

        // Enviar correo con PDF a todos los involucrad
        nombreCorreo.values().stream()
            .filter(correo -> correo != null && !correo.trim().isEmpty())
            .forEach(correo -> enviarCorreoReservaConPDF(correo, "Aquí está tu resumen de reserva", pdf));
        return reserva;
    }




}
