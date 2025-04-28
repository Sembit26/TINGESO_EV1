package com.example.demo.Services;

import com.example.demo.Entities.Comprobante;
import com.example.demo.Repositories.ComprobanteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ComprobanteServiceTest {

    @Mock
    private ComprobanteRepository comprobanteRepository;

    @InjectMocks
    private ComprobanteService comprobanteService;

    private Comprobante comprobante;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        comprobante = new Comprobante(1L, Arrays.asList("Detalle 1", "Detalle 2"), 10.0, 500.0, 95.0, 595.0);
    }

    // === Test de CRUD básico ===

    // === Test de findAll() ===
    @Test
    void testFindAll() {
        // Arrange
        when(comprobanteRepository.findAll()).thenReturn(Arrays.asList(comprobante));

        // Act
        List<Comprobante> result = comprobanteService.findAll();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(comprobante, result.get(0));

        // Verifica que el repositorio haya sido llamado correctamente
        verify(comprobanteRepository, times(1)).findAll();
    }

    // === Test de findById() ===

    @Test
    void testFindById_Found() {
        // Arrange
        when(comprobanteRepository.findById(1L)).thenReturn(Optional.of(comprobante));

        // Act
        Optional<Comprobante> result = comprobanteService.findById(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(comprobante, result.get());

        // Verifica que el repositorio haya sido llamado correctamente
        verify(comprobanteRepository, times(1)).findById(1L);
    }

    @Test
    void testFindById_NotFound() {
        // Arrange
        when(comprobanteRepository.findById(1L)).thenReturn(Optional.empty());

        // Act
        Optional<Comprobante> result = comprobanteService.findById(1L);

        // Assert
        assertFalse(result.isPresent());

        // Verifica que el repositorio haya sido llamado correctamente
        verify(comprobanteRepository, times(1)).findById(1L);
    }

    // === Test de save() ===

    @Test
    void testSave() {
        // Arrange
        when(comprobanteRepository.save(comprobante)).thenReturn(comprobante);

        // Act
        Comprobante result = comprobanteService.save(comprobante);

        // Assert
        assertNotNull(result);
        assertEquals(comprobante, result);

        // Verifica que el repositorio haya sido llamado correctamente
        verify(comprobanteRepository, times(1)).save(comprobante);
    }

    // === Test de deleteById() ===

    @Test
    void testDeleteById() {
        // Act
        comprobanteService.deleteById(1L);

        // Assert
        verify(comprobanteRepository, times(1)).deleteById(1L);
    }

    // === Test de update() ===

    @Test
    void testUpdate_Found() {
        // Arrange
        Comprobante updatedComprobante = new Comprobante(1L, Arrays.asList("Nuevo Detalle"), 5.0, 450.0, 85.5, 535.5);
        when(comprobanteRepository.findById(1L)).thenReturn(Optional.of(comprobante));
        when(comprobanteRepository.save(any(Comprobante.class))).thenReturn(updatedComprobante);

        // Act
        Comprobante result = comprobanteService.update(1L, updatedComprobante);

        // Assert
        assertNotNull(result);
        assertEquals(updatedComprobante.getPrecio_final(), result.getPrecio_final());

        // Verifica que el repositorio haya sido llamado correctamente
        verify(comprobanteRepository, times(1)).findById(1L);
        verify(comprobanteRepository, times(1)).save(any(Comprobante.class));
    }

    @Test
    void testUpdate_NotFound() {
        // Arrange
        Comprobante updatedComprobante = new Comprobante(1L, Arrays.asList("Nuevo Detalle"), 5.0, 450.0, 85.5, 535.5);
        when(comprobanteRepository.findById(1L)).thenReturn(Optional.empty());

        // Act
        Comprobante result = comprobanteService.update(1L, updatedComprobante);

        // Assert
        assertNull(result);

        // Verifica que el repositorio haya sido llamado correctamente
        verify(comprobanteRepository, times(1)).findById(1L);
        verify(comprobanteRepository, times(0)).save(any(Comprobante.class)); // No debe llamar a save cuando no encuentra el comprobante
    }

    // === Test de lógica de cálculo de comprobante ===

    @Test
    void testDescuentoGrupal_11a15Personas_SinDescuentoCumpleanero() {
        Map<String, String> nombreCorreo = new HashMap<>();
        for (int i = 1; i <= 12; i++) {
            nombreCorreo.put("C" + i, "c" + i + "@mail.com");
        }

        Comprobante r = comprobanteService.crearComprobante(20000, 12, 1, "C1", "c1@mail.com", nombreCorreo, List.of());

        assertEquals(12, r.getDetallePagoPorPersona().size());
    }

    //SEPARACION----------------------------------------------------------------------

    @Test
    void testSinDescuento() {
        ComprobanteService service = new ComprobanteService();

        int precioRegular = 10000;
        int numPersonas = 1;
        int frecuenciaCliente = 0;
        String nombreCliente = "Cliente Único";
        String correoCliente = "unico@mail.com";

        Map<String, String> grupo = Map.of(
                "Cliente Único", "unico@mail.com" // Solo el cliente
        );

        List<String> cumpleaneros = List.of(); // Nadie cumple años

        Comprobante comprobante = comprobanteService.crearComprobante(
                precioRegular,
                numPersonas,
                frecuenciaCliente,
                nombreCliente,
                correoCliente,
                grupo,
                cumpleaneros
        );

        // El monto final sin IVA debe ser exactamente el precio regular
        assertEquals(10000.0, comprobante.getPrecio_final());
    }


    @Test
    void testGrupo3a5ConUnCumple() {
        Comprobante comprobante = comprobanteService.crearComprobante(
                10000, 5, 1, "Cliente A", "a@mail.com",
                Map.of("Cliente A", "a@mail.com", "B", "b@mail.com", "C", "c@mail.com", "D", "d@mail.com", "E", "e@mail.com"),
                List.of("b@mail.com")
        );

        assertTrue(comprobante.getDetallePagoPorPersona().stream().anyMatch(s -> s.contains("Descuento Cumpleaños 50%")));
    }

    @Test
    void testGrupo6a10ConDosCumples() {
        Comprobante comprobante = comprobanteService.crearComprobante(
                10000, 7, 1, "Cliente A", "a@mail.com",
                Map.of(
                        "Cliente A", "a@mail.com",
                        "B", "b@mail.com",
                        "C", "c@mail.com",
                        "D", "d@mail.com",
                        "E", "e@mail.com",
                        "F", "f@mail.com",
                        "G", "g@mail.com"
                ),
                List.of("b@mail.com", "c@mail.com")
        );

        long cumpleCount = comprobante.getDetallePagoPorPersona().stream()
                .filter(s -> s.contains("Descuento Cumpleaños 50%")).count();
        assertEquals(2, cumpleCount);
    }

    @Test
    void testGrupo6a10ConMasDeDosCumples() {
        Comprobante comprobante = comprobanteService.crearComprobante(
                10000, 7, 1, "Cliente A", "a@mail.com",
                Map.of(
                        "Cliente A", "a@mail.com",
                        "B", "b@mail.com",
                        "C", "c@mail.com",
                        "D", "d@mail.com",
                        "E", "e@mail.com",
                        "F", "f@mail.com",
                        "G", "g@mail.com"
                ),
                List.of("b@mail.com", "c@mail.com", "d@mail.com")
        );

        long cumpleCount = comprobante.getDetallePagoPorPersona().stream()
                .filter(s -> s.contains("Descuento Cumpleaños 50%")).count();
        assertEquals(2, cumpleCount); // máximo permitido
    }

    @Test
    void testFrecuencia2a4SinCumple() {
        Comprobante comprobante = comprobanteService.crearComprobante(
                10000, 2, 3, "Cliente A", "a@mail.com",
                Map.of("Cliente A", "a@mail.com", "B", "b@mail.com"),
                List.of()
        );

        assertTrue(comprobante.getDetallePagoPorPersona().stream()
                .anyMatch(s -> s.contains("Descuento Frecuencia 10%")));
    }

    @Test
    void testFrecuencia5a6() {
        Comprobante comprobante = comprobanteService.crearComprobante(
                10000, 2, 5, "Cliente A", "a@mail.com",
                Map.of("Cliente A", "a@mail.com", "B", "b@mail.com"),
                List.of()
        );

        assertTrue(comprobante.getDetallePagoPorPersona().stream()
                .anyMatch(s -> s.contains("Descuento Frecuencia 20%")));
    }

    @Test
    void testFrecuenciaMayor7() {
        Comprobante comprobante = comprobanteService.crearComprobante(
                10000, 2, 8, "Cliente A", "a@mail.com",
                Map.of("Cliente A", "a@mail.com", "B", "b@mail.com"),
                List.of()
        );

        assertTrue(comprobante.getDetallePagoPorPersona().stream()
                .anyMatch(s -> s.contains("Descuento Frecuencia 30%")));
    }

    @Test
    void testClienteCumpleSinGrupoNiFrecuencia() {
        ComprobanteService service = new ComprobanteService();

        int precioRegular = 10000;
        int numPersonas = 3; // Para habilitar descuento de cumpleaños (al menos 3 personas)
        int frecuenciaCliente = 0; // No tiene frecuencia
        String nombreCliente = "Cliente A";
        String correoCliente = "a@mail.com";

        Map<String, String> grupo = Map.of(
                "Cliente A", "a@mail.com",
                "Persona 1", "p1@mail.com",
                "Persona 2", "p2@mail.com"
        );

        List<String> cumpleaneros = List.of("a@mail.com"); // Solo el cliente cumple

        Comprobante comprobante = comprobanteService.crearComprobante(
                precioRegular,
                numPersonas,
                frecuenciaCliente,
                nombreCliente,
                correoCliente,
                grupo,
                cumpleaneros
        );

        // Verificamos que haya descuento de cumpleaños para el cliente
        boolean clienteTieneDescuento = comprobante.getDetallePagoPorPersona().stream()
                .anyMatch(linea -> linea.contains("Cliente A") && linea.contains("Descuento Cumpleaños 50%"));

        assertTrue(clienteTieneDescuento, "El cliente debería tener descuento de cumpleaños.");
    }


    @Test
    void testClienteSinCumpleNiFrecuenciaPeroGrupoAplica() {
        Comprobante comprobante = comprobanteService.crearComprobante(
                10000, 4, 1, "Cliente A", "a@mail.com",
                Map.of("Cliente A", "a@mail.com", "B", "b@mail.com", "C", "c@mail.com", "D", "d@mail.com"),
                List.of()
        );

        assertTrue(comprobante.getDetallePagoPorPersona().stream()
                .anyMatch(s -> s.contains("Descuento Grupal 10%")));
    }

    @Test
    void testClienteConCumpleYFrecuenciaYGrupoPeroSoloCumpleAplica() {
        Comprobante comprobante = comprobanteService.crearComprobante(
                10000, 5, 5, "Cliente A", "a@mail.com",
                Map.of("Cliente A", "a@mail.com", "B", "b@mail.com", "C", "c@mail.com", "D", "d@mail.com", "E", "e@mail.com"),
                List.of("a@mail.com")
        );

        assertTrue(comprobante.getDetallePagoPorPersona().stream()
                .anyMatch(s -> s.contains("Descuento Cumpleaños 50%")));
    }

    // === Test de formateo de comprobante ===

    @Test
    void testFormatearComprobante() {
        // Arrange
        Comprobante comprobante = new Comprobante();
        comprobante.setPrecio_final(80000.00);
        comprobante.setIva(15200.00);
        comprobante.setMonto_total_iva(95200.00);
        comprobante.setDetallePagoPorPersona(Arrays.asList(
                "Cliente1|Base:20000.00|Descuento Grupal 20%|Monto sin IVA:16000.00|IVA:3040.00|Total:19040.00",
                "Cliente2|Base:20000.00|Descuento Grupal 20%|Monto sin IVA:16000.00|IVA:3040.00|Total:19040.00"
        ));

        // Act
        String result = comprobanteService.formatearComprobante(comprobante);

        // Assert
        String expectedOutput = "========= RESUMEN DEL COMPROBANTE =========\n" +
                "Subtotal (sin IVA): 80000.00\n" +
                "IVA: 15200.00\n" +
                "Total con IVA: 95200.00\n" +
                "-------------------------------------------\n" +
                "Detalle por persona:\n\n" +
                "- Cliente1\n" +
                "  Precio Base (sin IVA): 20000.00\n" +
                "  Descuento Grupal 20%\n" +
                "  Monto sin IVA: 16000.00\n" +
                "  IVA: 3040.00\n" +
                "  Total: 19040.00\n\n" +
                "- Cliente2\n" +
                "  Precio Base (sin IVA): 20000.00\n" +
                "  Descuento Grupal 20%\n" +
                "  Monto sin IVA: 16000.00\n" +
                "  IVA: 3040.00\n" +
                "  Total: 19040.00\n\n" +
                "===========================================\n";

        assertEquals(expectedOutput, result);
    }
}
