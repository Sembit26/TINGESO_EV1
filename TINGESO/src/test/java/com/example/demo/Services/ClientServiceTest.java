package com.example.demo.Services;

import com.example.demo.Entities.Client;
import com.example.demo.Entities.Reserva;
import com.example.demo.Repositories.ClientRepository;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.AfterEach;
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

class ClientServiceTest {

    @InjectMocks
    private ClientService clientService;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private MimeMessage mimeMessage;

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private ReservaService reservaService;

    private File archivoGenerado;

    private final String resumenTexto = "Este es un resumen de la reserva";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() {
        if (archivoGenerado != null && archivoGenerado.exists()) {
            archivoGenerado.delete();
        }
    }

    // ======= CRUD =======

    @Test
    void testFindAll() {
        List<Client> clients = Arrays.asList(new Client(), new Client());
        when(clientRepository.findAll()).thenReturn(clients);

        List<Client> result = clientService.findAll();
        assertEquals(2, result.size());
    }

    @Test
    void testFindById() {
        // Arrange
        Long clientId = 1L;

        Client client = new Client();
        client.setId(clientId);
        client.setName("Test User");

        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));

        // Act
        Optional<Client> result = clientService.findById(clientId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("Test User", result.get().getName());
    }


    @Test
    void testFindByIdNotFound() {
        when(clientRepository.findById(1L)).thenReturn(null);

        Optional<Client> result = clientService.findById(1L);

        assertFalse(result.isPresent());
    }


    @Test
    void testFindByEmail() {
        Client client = new Client();
        when(clientRepository.findByEmail("test@example.com")).thenReturn(client);

        Client result = clientService.findByEmail("test@example.com");
        assertNotNull(result);
    }

    @Test
    void testFindByRut() {
        Client client = new Client();
        when(clientRepository.findByRut("12345678-9")).thenReturn(client);

        Client result = clientService.findByRut("12345678-9");
        assertNotNull(result);
    }

    @Test
    void testSave() {
        Client client = new Client();
        when(clientRepository.save(client)).thenReturn(client);

        Client result = clientService.save(client);
        assertEquals(client, result);
    }

    @Test
    void testDeleteById() {
        clientService.deleteById(1L);
        verify(clientRepository, times(1)).deleteById(1L);
    }

    @Test
    void testUpdate_ClienteExiste() {
        // Arrange
        Long clientId = 1L;
        Client existingClient = new Client();
        existingClient.setId(clientId);
        existingClient.setName("Juan Pérez");
        existingClient.setEmail("juan@mail.com");
        existingClient.setContrasena("1234");
        existingClient.setRut("12345678-9");
        existingClient.setBirthday(LocalDate.of(1990, 1, 1));
        existingClient.setNum_visitas_al_mes(2);

        Client updatedClient = new Client();
        updatedClient.setName("Juan Actualizado");
        updatedClient.setEmail("nuevojuan@mail.com");
        updatedClient.setContrasena("5678");
        updatedClient.setRut("98765432-1");
        updatedClient.setBirthday(LocalDate.of(1992, 2, 2));
        updatedClient.setNum_visitas_al_mes(5);

        when(clientRepository.findById(clientId)).thenReturn(Optional.of(existingClient));
        when(clientRepository.save(any(Client.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Client result = clientService.update(clientId, updatedClient);

        // Assert
        assertNotNull(result);
        assertEquals("Juan Actualizado", result.getName());
        assertEquals("nuevojuan@mail.com", result.getEmail());
        assertEquals("5678", result.getContrasena());
        assertEquals("98765432-1", result.getRut());
        assertEquals(LocalDate.of(1992, 2, 2), result.getBirthday());
        assertEquals(5, result.getNum_visitas_al_mes());
    }

    @Test
    void testUpdate_ClienteNoExiste() {
        // Arrange
        Long clientId = 999L;
        Client updatedClient = new Client();
        updatedClient.setName("Nuevo Nombre");

        when(clientRepository.findById(clientId)).thenReturn(Optional.empty());

        // Act
        Client result = clientService.update(clientId, updatedClient);

        // Assert
        assertNull(result);
    }



    // ======= Registro y Login =======

    @Test
    void testRegister_clienteYaExistePorEmail_lanzaExcepcion() {
        // Arrange
        when(clientService.findByEmail("juan@example.com")).thenReturn(new Client());

        // Act + Assert
        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            clientService.register("12345678-9", "Juan", "juan@example.com", "1234", LocalDate.of(2000, 1, 1));
        });

        assertEquals("El cliente con el correo o RUT ya existe.", ex.getMessage());
    }

    @Test
    void testRegister_clienteYaExistePorRut_lanzaExcepcion() {
        // Arrange
        when(clientService.findByEmail("nuevo@example.com")).thenReturn(null);
        when(clientService.findByRut("11111111-1")).thenReturn(new Client());

        // Act + Assert
        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            clientService.register("11111111-1", "Pedro", "nuevo@example.com", "1234", LocalDate.of(1999, 5, 5));
        });

        assertEquals("El cliente con el correo o RUT ya existe.", ex.getMessage());
    }

    @Test
    void testRegister_clienteNuevo_registroExitoso() {
        // Arrange
        when(clientService.findByEmail("nuevo@example.com")).thenReturn(null);
        when(clientService.findByRut("22222222-2")).thenReturn(null);

        Client guardado = new Client();
        guardado.setEmail("nuevo@example.com");

        when(clientService.save(any(Client.class))).thenReturn(guardado);

        // Act
        Client resultado = clientService.register("22222222-2", "Ana", "nuevo@example.com", "abcd", LocalDate.of(2001, 6, 6));

        // Assert
        assertEquals("nuevo@example.com", resultado.getEmail());
    }

    @Test
    void testLogin_clienteNoExiste_lanzaExcepcion() {
        when(clientService.findByEmail("noexiste@example.com")).thenReturn(null);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            clientService.login("noexiste@example.com", "1234");
        });

        assertEquals("No se encontró el cliente con el email noexiste@example.com", ex.getMessage());
    }

    @Test
    void testLogin_contraseniaIncorrecta_lanzaExcepcion() {
        Client cliente = new Client();
        cliente.setEmail("juan@example.com");
        cliente.setContrasena("correcta");

        when(clientService.findByEmail("juan@example.com")).thenReturn(cliente);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            clientService.login("juan@example.com", "incorrecta");
        });

        assertEquals("Contraseña incorrecta", ex.getMessage());
    }

    @Test
    void testLogin_loginNuevoMes_reseteaVisitas() {
        Client cliente = new Client();
        cliente.setEmail("ana@example.com");
        cliente.setContrasena("clave123");
        cliente.setLastLoginDate(LocalDate.now().minusMonths(1));
        cliente.setNum_visitas_al_mes(5);

        when(clientService.findByEmail("ana@example.com")).thenReturn(cliente);
        when(clientService.save(any(Client.class))).thenReturn(cliente);

        Client resultado = clientService.login("ana@example.com", "clave123");

        assertEquals(LocalDate.now(), resultado.getLastLoginDate());
        assertEquals(0, resultado.getNum_visitas_al_mes());
    }

    @Test
    void testLogin_actualizarVisitas_mesYannoDiferentes() {
        // Caso 1: El cliente tiene un último login en un mes y año diferentes
        Client client = new Client();
        client.setEmail("juan@example.com");
        client.setContrasena("password123");
        client.setNum_visitas_al_mes(5);
        client.setLastLoginDate(LocalDate.of(2024, 3, 15)); // Login anterior fue en marzo

        when(clientRepository.findByEmail("juan@example.com")).thenReturn(client);

        // Ejecutar el login
        Client resultado = clientService.login("juan@example.com", "password123");

        // Verificar que el contador de visitas se reinicia
        assertEquals(0, client.getNum_visitas_al_mes());
        assertEquals(LocalDate.now(), client.getLastLoginDate());
        verify(clientRepository).save(client);
    }

    @Test
    void testLogin_actualizarVisitas_mesYannoIguales() {
        // Caso 2: El cliente tiene un último login en el mismo mes y año
        Client client = new Client();
        client.setEmail("juan@example.com");
        client.setContrasena("password123");
        client.setNum_visitas_al_mes(5);
        client.setLastLoginDate(LocalDate.now()); // Login anterior fue este mes y año

        when(clientRepository.findByEmail("juan@example.com")).thenReturn(client);

        // Ejecutar el login
        Client resultado = clientService.login("juan@example.com", "password123");

        // Verificar que el contador de visitas no se reinicia
        assertEquals(5, client.getNum_visitas_al_mes());
        assertEquals(LocalDate.now(), client.getLastLoginDate());
        verify(clientRepository).save(client);
    }

    @Test
    void testLogin_actualizarVisitas_loginNulo() {
        // Caso 3: El cliente no tiene un último login (es nulo)
        Client client = new Client();
        client.setEmail("juan@example.com");
        client.setContrasena("password123");
        client.setNum_visitas_al_mes(5);
        client.setLastLoginDate(null); // Sin último login

        when(clientRepository.findByEmail("juan@example.com")).thenReturn(client);

        // Ejecutar el login
        Client resultado = clientService.login("juan@example.com", "password123");

        // Verificar que el contador de visitas se reinicia
        assertEquals(0, client.getNum_visitas_al_mes());
        assertEquals(LocalDate.now(), client.getLastLoginDate());
        verify(clientRepository).save(client);
    }


    // ======= PDF y Correo =======

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
        clientService.enviarCorreoReservaConPDF(correo, cuerpo, archivoPdf);

        // Assert
        verify(mailSender, times(1)).createMimeMessage();
        verify(mailSender, times(1)).send(mimeMessageMock);
    }




    // ======= Generar Reserva =======



    @Test
    void testGenerarReserva_CubreTodasLasRamas() throws Exception {
        // Setup datos de entrada
        Long clientId = 1L;
        LocalDate birthday = LocalDate.of(2000, 4, 22);
        LocalDate fechaInicio = LocalDate.of(2025, 4, 22); // Coincide con cumpleaños
        LocalTime horaInicio = LocalTime.of(10, 0);

        Client cliente = new Client();
        cliente.setId(clientId);
        cliente.setName("Juan");
        cliente.setEmail("juan@example.com");
        cliente.setBirthday(birthday);
        cliente.setNum_visitas_al_mes(2);
        cliente.setReservas(new ArrayList<>());

        // Correos cumpleañeros
        List<String> correosCumple = new ArrayList<>();
        correosCumple.add("cumple1@example.com");
        correosCumple.add("invalido@example.com"); // Este será eliminado

        // Mock: cliente principal
        when(clientRepository.findById(clientId)).thenReturn(Optional.of(cliente));

        // Mock: cumpleañero válido
        Client cumpleanero = new Client();
        cumpleanero.setEmail("cumple1@example.com");
        cumpleanero.setBirthday(LocalDate.of(1999, 4, 22));
        when(clientRepository.findByEmail("cumple1@example.com")).thenReturn(cumpleanero);

        // Mock: cumpleañero inválido (sin fecha)
        Client sinFecha = new Client();
        sinFecha.setEmail("invalido@example.com");
        sinFecha.setBirthday(null);
        when(clientRepository.findByEmail("invalido@example.com")).thenReturn(sinFecha);

        // Mock: creación de reserva
        Reserva reserva = new Reserva();
        when(reservaService.crearReserva(anyInt(), anyInt(), anyList(), any(), any(), anyInt(), any(), any(), anyMap()))
                .thenReturn(reserva);

        // Mock: resumen PDF
        when(reservaService.obtenerInformacionReservaConComprobante(any())).thenReturn("RESUMEN PDF");

        // Mock: correo
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        Map<String, String> nombreCorreo = new HashMap<>();

        // Ejecutar
        Reserva resultado = clientService.generarReserva(
                clientId,
                5,
                3,
                correosCumple,
                fechaInicio,
                horaInicio,
                nombreCorreo
        );

        // Verificaciones
        assertEquals(reserva, resultado);
        assertEquals(1, cliente.getReservas().size()); // Se agregó
        assertEquals(3, cliente.getNum_visitas_al_mes()); // Incrementado

        // Solo se mantuvo el cumpleañero con fecha válida
        assertEquals(1, correosCumple.size());
        assertTrue(correosCumple.contains("cumple1@example.com"));

        verify(reservaService).crearReserva(
                eq(5), eq(3), eq(correosCumple),
                eq(fechaInicio), eq(horaInicio),
                eq(2), eq("Juan"), eq("juan@example.com"),
                anyMap()
        );

        verify(mailSender, atLeastOnce()).createMimeMessage();
        verify(clientRepository).save(cliente);
    }

    @Test
    void testGenerarReserva_clienteNoExiste_lanzaExcepcion() {
        when(clientRepository.findById(999L)).thenReturn(null);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            clientService.generarReserva(
                    999L,
                    2,
                    2,
                    new ArrayList<>(),
                    LocalDate.now(),
                    LocalTime.now(),
                    new HashMap<>()
            );
        });

        assertEquals("Cliente no encontrado", ex.getMessage());
    }

    @Test
    void testGenerarReserva_EliminaCumpleaneroConFechaDiferenteYCorreoNulo() throws Exception {
        // Datos de entrada
        Long clientId = 1L;
        LocalDate fechaInicio = LocalDate.of(2025, 4, 22);
        LocalTime horaInicio = LocalTime.of(10, 0);

        Client cliente = new Client();
        cliente.setId(clientId);
        cliente.setName("Maria");
        cliente.setEmail("maria@example.com");
        cliente.setNum_visitas_al_mes(1);
        cliente.setReservas(new ArrayList<>());

        List<String> correosCumple = new ArrayList<>();
        correosCumple.add("cumpleInvalido@example.com");

        // Mock cliente principal
        when(clientRepository.findById(clientId)).thenReturn(Optional.of(cliente));

        // Mock cumpleañero con fecha válida, pero que NO coincide
        Client cumpleanero = new Client();
        cumpleanero.setEmail("cumpleInvalido@example.com");
        cumpleanero.setBirthday(LocalDate.of(1990, 1, 1)); // no coincide mes/día
        when(clientRepository.findByEmail("cumpleInvalido@example.com")).thenReturn(cumpleanero);

        // Mock reserva
        Reserva reserva = new Reserva();
        when(reservaService.crearReserva(anyInt(), anyInt(), anyList(), any(), any(), anyInt(), any(), any(), anyMap()))
                .thenReturn(reserva);

        // Mock resumen y correo
        when(reservaService.obtenerInformacionReservaConComprobante(any())).thenReturn("Resumen PDF");
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        // Map con cliente válido y uno con correo nulo
        Map<String, String> nombreCorreo = new HashMap<>();
        nombreCorreo.put("Maria", "maria@example.com");
        nombreCorreo.put("Inválido", null); // Para probar el filtro de correos nulos

        // Ejecutar
        Reserva resultado = clientService.generarReserva(
                clientId,
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
        assertEquals(2, cliente.getNum_visitas_al_mes());
        assertEquals(1, cliente.getReservas().size());

        // Solo debe enviar un correo (el válido)
        verify(mailSender, atLeastOnce()).createMimeMessage();
        verify(clientRepository).save(cliente);
        verify(reservaService).crearReserva(
                eq(2), eq(2), eq(Collections.emptyList()),
                eq(fechaInicio), eq(horaInicio),
                eq(1), eq("Maria"), eq("maria@example.com"),
                anyMap()
        );
    }

    @Test
    void testGenerarReserva_EliminaCumpleaneroConDiaDistinto() throws Exception {
        Long clientId = 2L;
        LocalDate fechaInicio = LocalDate.of(2025, 4, 22); // abril 22
        LocalTime horaInicio = LocalTime.of(9, 0);

        Client cliente = new Client();
        cliente.setId(clientId);
        cliente.setName("Pedro");
        cliente.setEmail("pedro@example.com");
        cliente.setNum_visitas_al_mes(0);
        cliente.setReservas(new ArrayList<>());

        List<String> correosCumple = new ArrayList<>();
        correosCumple.add("diaDistinto@example.com");

        // Mock cliente principal
        when(clientRepository.findById(clientId)).thenReturn(Optional.of(cliente));

        // Mock cumpleañero con mismo mes (abril) pero día distinto
        Client cumpleanero = new Client();
        cumpleanero.setEmail("diaDistinto@example.com");
        cumpleanero.setBirthday(LocalDate.of(2000, 4, 10)); // abril 10
        when(clientRepository.findByEmail("diaDistinto@example.com")).thenReturn(cumpleanero);

        // Mock reserva
        Reserva reserva = new Reserva();
        when(reservaService.crearReserva(anyInt(), anyInt(), anyList(), any(), any(), anyInt(), any(), any(), anyMap()))
                .thenReturn(reserva);

        when(reservaService.obtenerInformacionReservaConComprobante(any())).thenReturn("Resumen PDF");
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        Map<String, String> nombreCorreo = new HashMap<>();
        nombreCorreo.put("Pedro", "pedro@example.com");

        Reserva resultado = clientService.generarReserva(
                clientId, 1, 1, correosCumple, fechaInicio, horaInicio, nombreCorreo
        );

        assertEquals(reserva, resultado);
        assertTrue(correosCumple.isEmpty(), "Debe eliminar al cumpleañero cuyo día no coincide aunque el mes sí");
        verify(clientRepository).save(cliente);
    }

    @Test
    void testGenerarReserva_CumpleaneroConBirthdayNullEsEliminado() throws Exception {
        Long clientId = 4L;
        LocalDate fechaInicio = LocalDate.of(2025, 4, 22);
        LocalTime horaInicio = LocalTime.of(14, 0);

        Client cliente = new Client();
        cliente.setId(clientId);
        cliente.setName("Lucia");
        cliente.setEmail("lucia@example.com");
        cliente.setNum_visitas_al_mes(0);
        cliente.setReservas(new ArrayList<>());

        List<String> correosCumple = new ArrayList<>();
        correosCumple.add("sinFecha@example.com");

        // Cliente principal
        when(clientRepository.findById(clientId)).thenReturn(Optional.of(cliente));

        // Mock cumpleañero con birthday == null
        Client cumpleanero = new Client();
        cumpleanero.setEmail("sinFecha@example.com");
        cumpleanero.setBirthday(null); // <- Caso que queremos probar
        when(clientRepository.findByEmail("sinFecha@example.com")).thenReturn(cumpleanero);

        // Reserva mock
        Reserva reserva = new Reserva();
        when(reservaService.crearReserva(anyInt(), anyInt(), anyList(), any(), any(), anyInt(), any(), any(), anyMap()))
                .thenReturn(reserva);

        when(reservaService.obtenerInformacionReservaConComprobante(any())).thenReturn("Resumen PDF");
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        Map<String, String> nombreCorreo = new HashMap<>();
        nombreCorreo.put("Lucia", "lucia@example.com");

        Reserva resultado = clientService.generarReserva(
                clientId, 2, 2, correosCumple, fechaInicio, horaInicio, nombreCorreo
        );

        assertEquals(reserva, resultado);
        assertTrue(correosCumple.isEmpty(), "Debe eliminar al cumpleañero si su birthday es null");
        verify(clientRepository).save(cliente);
    }

    @Test
    void testGenerarReserva_CumpleaneroNoEncontradoEsEliminado() throws Exception {
        Long clientId = 5L;
        LocalDate fechaInicio = LocalDate.of(2025, 4, 22);
        LocalTime horaInicio = LocalTime.of(15, 0);

        Client cliente = new Client();
        cliente.setId(clientId);
        cliente.setName("Carlos");
        cliente.setEmail("carlos@example.com");
        cliente.setNum_visitas_al_mes(1);
        cliente.setReservas(new ArrayList<>());

        List<String> correosCumple = new ArrayList<>();
        correosCumple.add("noexiste@example.com");

        // Cliente principal
        when(clientRepository.findById(clientId)).thenReturn(Optional.of(cliente));

        // Mock: cumpleañero no encontrado
        when(clientRepository.findByEmail("noexiste@example.com")).thenReturn(null); // <- Este es el caso que queremos testear

        // Reserva mock
        Reserva reserva = new Reserva();
        when(reservaService.crearReserva(anyInt(), anyInt(), anyList(), any(), any(), anyInt(), any(), any(), anyMap()))
                .thenReturn(reserva);

        when(reservaService.obtenerInformacionReservaConComprobante(any())).thenReturn("Resumen PDF");
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        Map<String, String> nombreCorreo = new HashMap<>();
        nombreCorreo.put("Carlos", "carlos@example.com");

        Reserva resultado = clientService.generarReserva(
                clientId, 3, 2, correosCumple, fechaInicio, horaInicio, nombreCorreo
        );

        assertEquals(reserva, resultado);
        assertTrue(correosCumple.isEmpty(), "Debe eliminar al cumpleañero si no se encuentra en la base");
        verify(clientRepository).save(cliente);
    }
}


