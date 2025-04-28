package com.example.demo.Services;

import com.example.demo.Entities.Client;
import com.example.demo.Entities.Empleado;
import com.example.demo.Entities.Reserva;
import com.example.demo.Repositories.EmpleadoRepository;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.mail.javamail.JavaMailSender;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class EmpleadoServiceTest {

    @Mock
    private EmpleadoRepository empleadoRepository;

    @InjectMocks
    private EmpleadoService empleadoService;

    @Mock
    private ReservaService reservaService;

    @Mock
    private ClientService clientService;

    private Empleado empleado;

    @Mock
    private MimeMessage mimeMessage;

    @Mock
    private JavaMailSender mailSender;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        empleado = new Empleado(1L, "12345678-9", "Juan Pérez", "juan@example.com", "contrasena123");
    }

    @Test
    void testGetAllEmpleados() {
        List<Empleado> empleados = List.of(empleado);
        when(empleadoRepository.findAll()).thenReturn(empleados);

        List<Empleado> resultado = empleadoService.getAllEmpleados();

        assertEquals(1, resultado.size());
        verify(empleadoRepository).findAll();
    }

    @Test
    void testFindById_EmpleadoExiste() {
        // Arrange
        Long empleadoId = 1L;

        Empleado empleado = new Empleado();
        empleado.setId(empleadoId);
        empleado.setName("Empleado Test");

        when(empleadoRepository.findById(empleadoId)).thenReturn(Optional.of(empleado));

        // Act
        Optional<Empleado> result = empleadoService.getEmpleadoById(empleadoId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("Empleado Test", result.get().getName());
    }

    @Test
    void testFindById_EmpleadoNoExiste() {
        Long empleadoId = 999L;

        when(empleadoRepository.findById(empleadoId)).thenReturn(Optional.empty());

        // Act
        Optional<Empleado> result = empleadoService.getEmpleadoById(empleadoId);

        // Assert
        assertFalse(result.isPresent());
    }


    @Test
    void testSaveEmpleado() {
        when(empleadoRepository.save(empleado)).thenReturn(empleado);

        Empleado resultado = empleadoService.save(empleado);

        assertNotNull(resultado);
        assertEquals("juan@example.com", resultado.getEmail());
        verify(empleadoRepository).save(empleado);
    }

    @Test
    void testUpdate_EmpleadoExiste() {
        // Arrange
        Long empleadoId = 1L;

        Empleado existingEmpleado = new Empleado();
        existingEmpleado.setId(empleadoId);
        existingEmpleado.setName("Juan Pérez");
        existingEmpleado.setEmail("juan@mail.com");
        existingEmpleado.setContrasena("1234");
        existingEmpleado.setRut("12345678-9");

        Empleado updatedEmpleado = new Empleado();
        updatedEmpleado.setName("Pedro Gómez");
        updatedEmpleado.setEmail("pedro@mail.com");
        updatedEmpleado.setContrasena("nueva123");
        updatedEmpleado.setRut("98765432-1");

        when(empleadoRepository.findById(empleadoId)).thenReturn(Optional.of(existingEmpleado));
        when(empleadoRepository.save(any(Empleado.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Empleado result = empleadoService.updateEmpleado(empleadoId, updatedEmpleado);

        // Assert
        assertNotNull(result);
        assertEquals("Pedro Gómez", result.getName());
        assertEquals("pedro@mail.com", result.getEmail());
        assertEquals("nueva123", result.getContrasena());
        assertEquals("98765432-1", result.getRut());
    }



    @Test
    void testUpdateEmpleadoNoExistente() {
        when(empleadoRepository.findById(2L)).thenReturn(null);

        Empleado resultado = empleadoService.updateEmpleado(2L, empleado);

        assertNull(resultado);
        verify(empleadoRepository, never()).save(any());
    }

    @Test
    void testDeleteEmpleadoExistente() {
        when(empleadoRepository.existsById(1L)).thenReturn(true);
        doNothing().when(empleadoRepository).deleteById(1L);

        boolean resultado = empleadoService.deleteEmpleado(1L);

        assertTrue(resultado);
        verify(empleadoRepository).deleteById(1L);
    }

    @Test
    void testDeleteEmpleadoNoExistente() {
        when(empleadoRepository.existsById(1L)).thenReturn(false);

        boolean resultado = empleadoService.deleteEmpleado(1L);

        assertFalse(resultado);
        verify(empleadoRepository, never()).deleteById(1L);
    }

    @Test
    void testFindByEmail() {
        when(empleadoRepository.findByEmail("juan@example.com")).thenReturn(empleado);

        Empleado resultado = empleadoService.findByEmail("juan@example.com");

        assertNotNull(resultado);
        assertEquals("Juan Pérez", resultado.getName());
        verify(empleadoRepository).findByEmail("juan@example.com");
    }

    @Test
    void testFindByRut() {
        when(empleadoRepository.findByRut("12345678-9")).thenReturn(empleado);

        Empleado resultado = empleadoService.findByRut("12345678-9");

        assertNotNull(resultado);
        assertEquals("Juan Pérez", resultado.getName());
        verify(empleadoRepository).findByRut("12345678-9");
    }

    @Test
    void testRegister_EmpleadoYaExistePorEmail_lanzaExcepcion() {
        // Arrange
        when(empleadoService.findByEmail("juan@example.com")).thenReturn(new Empleado());

        // Act + Assert
        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            empleadoService.register("12345678-9", "Juan", "juan@example.com", "1234");
        });

        assertEquals("El empleado con el correo o RUT ya existe.", ex.getMessage());
    }

    @Test
    void testRegister_EmpleadoYaExistePorRut_lanzaExcepcion() {
        // Arrange
        when(empleadoService.findByEmail("nuevo@example.com")).thenReturn(null);
        when(empleadoService.findByRut("11111111-1")).thenReturn(new Empleado());

        // Act + Assert
        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            empleadoService.register("11111111-1", "Pedro", "nuevo@example.com", "1234");
        });

        assertEquals("El empleado con el correo o RUT ya existe.", ex.getMessage());
    }

    @Test
    void testRegister_EmpleadoNuevo_registroExitoso() {
        // Arrange
        when(empleadoService.findByEmail("nuevo@example.com")).thenReturn(null);
        when(empleadoService.findByRut("22222222-2")).thenReturn(null);

        Empleado guardado = new Empleado();
        guardado.setEmail("nuevo@example.com");

        when(empleadoService.save(any(Empleado.class))).thenReturn(guardado);

        // Act
        Empleado resultado = empleadoService.register("22222222-2", "Ana", "nuevo@example.com", "abcd");

        // Assert
        assertEquals("nuevo@example.com", resultado.getEmail());
    }

    @Test
    void testLogin_ContraseñaIncorrecta_lanzaExcepcion() {
        Empleado empleado = new Empleado();
        empleado.setEmail("juan@example.com");
        empleado.setContrasena("correcta");

        when(empleadoService.findByEmail("juan@example.com")).thenReturn(empleado);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            empleadoService.login("juan@example.com", "incorrecta");
        });

        assertEquals("Contraseña incorrecta", ex.getMessage());
    }

    @Test
    void testLogin_EmpleadoLoginExitoso() {
        Empleado empleado = new Empleado();
        empleado.setEmail("juan@example.com");
        empleado.setContrasena("correcta");

        when(empleadoService.findByEmail("juan@example.com")).thenReturn(empleado);

        // Act
        Empleado result = empleadoService.login("juan@example.com", "correcta");

        // Assert
        assertNotNull(result);
        assertEquals("juan@example.com", result.getEmail());
    }

    @Test
    void testLogin_empleadoNoExiste_lanzaExcepcion() {
        when(empleadoService.findByEmail("noexiste@example.com")).thenReturn(null);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            empleadoService.login("noexiste@example.com", "1234");
        });

        assertEquals("No se encontró el empleado con el email noexiste@example.com", ex.getMessage());
    }


    @Test
    void testEnviarCorreoReservaConPDF_enviaCorreoCorrectamente() throws Exception {
        // Arrange
        String correo = "juan@example.com";
        String cuerpo = "<p>Este es el cuerpo del correo.</p>";
        File archivoPdf = new File("dummy.pdf");

        // Creamos un dummy MimeMessage
        MimeMessage mimeMessageMock = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessageMock);

        // Act
        empleadoService.enviarCorreoReservaConPDF(correo, cuerpo, archivoPdf);

        // Assert
        verify(mailSender, times(1)).createMimeMessage();
        verify(mailSender, times(1)).send(mimeMessageMock);
    }

    @Test
    void testGenerarReserva_EliminaCumpleaneroConFechaDiferenteYCorreoNulo() throws Exception {
        // Datos de entrada
        String nombreCliente = "Carlos";
        String correoCliente = "carlos@example.com";
        LocalDate fechaInicio = LocalDate.of(2025, 4, 22);
        LocalTime horaInicio = LocalTime.of(10, 0);

        // Lista de correos de cumpleañeros (uno inválido)
        List<String> correosCumple = new ArrayList<>();
        correosCumple.add("cumpleInvalido@example.com");

        // Mock cliente principal
        Client cliente = new Client();
        cliente.setName(nombreCliente);
        cliente.setEmail(correoCliente);
        cliente.setBirthday(LocalDate.of(1990, 4, 22)); // Cumpleaños coincide

        // Mock cumpleañero con fecha válida, pero que NO coincide
        Client cumpleanero = new Client();
        cumpleanero.setEmail("cumpleInvalido@example.com");
        cumpleanero.setBirthday(LocalDate.of(1990, 1, 1)); // no coincide mes/día
        when(clientService.findByEmail("cumpleInvalido@example.com")).thenReturn(cumpleanero);

        // Mock reserva
        Reserva reserva = new Reserva();
        when(reservaService.crearReserva(anyInt(), anyInt(), anyList(), any(), any(), anyInt(), any(), any(), anyMap()))
                .thenReturn(reserva);

        // Mock resumen y correo
        when(reservaService.obtenerInformacionReservaConComprobante(any())).thenReturn("Resumen PDF");
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        // Mapa con cliente principal
        Map<String, String> nombreCorreo = new HashMap<>();
        nombreCorreo.put(nombreCliente, correoCliente);

        // Ejecutar
        Reserva resultado = empleadoService.generarReserva(
                nombreCliente,
                correoCliente,
                2,
                2,
                correosCumple,
                fechaInicio,
                horaInicio,
                nombreCorreo
        );

        // Verificar
        assertEquals(reserva, resultado);
        assertTrue(correosCumple.isEmpty(), "Debe eliminar al cumpleañero cuya fecha no coincide");
        assertTrue(nombreCorreo.containsKey(nombreCliente), "El cliente debe estar en el mapa con su correo");

        // Solo debe enviar un correo (el válido)
        verify(mailSender, atLeastOnce()).createMimeMessage();
        verify(reservaService).crearReserva(
                eq(2), eq(2), eq(Collections.emptyList()),
                eq(fechaInicio), eq(horaInicio),
                eq(0), eq(nombreCliente), eq(correoCliente),
                eq(nombreCorreo)
        );
    }

}