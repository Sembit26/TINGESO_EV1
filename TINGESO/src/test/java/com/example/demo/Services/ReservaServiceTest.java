package com.example.demo.Services;

import com.example.demo.Entities.Comprobante;
import com.example.demo.Entities.Kart;
import com.example.demo.Entities.Reserva;
import com.example.demo.Repositories.ReservaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ReservaServiceTest {

    @Mock
    private ReservaRepository reservaRepository;

    @InjectMocks
    private ReservaService reservaService;

    @Mock
    private ComprobanteService comprobanteService;

    @Mock
    private KartService kartService;

    private Reserva reserva;

    @BeforeEach
    public void setUp() {
        // Inicializar los mocks antes de cada test
        MockitoAnnotations.openMocks(this);

        // Inicialización de los objetos que vas a usar en los tests
        reserva = new Reserva();
        reserva.setId(1L);
        reserva.setFechaHora(LocalDateTime.of(2025, 5, 1, 10, 0));
        reserva.setFechaInicio(LocalDate.of(2025, 5, 1));
        reserva.setHoraInicio(LocalTime.of(10, 0));
        reserva.setHoraFin(LocalTime.of(10, 20));
        reserva.setNum_vueltas_tiempo_maximo(5);
        reserva.setNum_personas(1);
        reserva.setNombreCliente("Cliente Único");


    }


    @Test
    void testFindAll() {
        // Arrange
        when(reservaRepository.findAll()).thenReturn(Arrays.asList(reserva));

        // Act
        var result = reservaService.findAll();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(reserva, result.get(0));
    }

    @Test
    void testFindById_Found() {
        // Arrange
        when(reservaRepository.findById(1L)).thenReturn(Optional.of(reserva));

        // Act
        Optional<Reserva> result = reservaService.findById(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(reserva, result.get());
    }

    @Test
    void testFindById_NotFound() {
        // Arrange
        when(reservaRepository.findById(1L)).thenReturn(Optional.empty());

        // Act
        Optional<Reserva> result = reservaService.findById(1L);

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    void testGetReservasByFechaInicio() {
        // Arrange
        LocalDate fecha = LocalDate.now();
        when(reservaRepository.findByFechaInicioOrderByHoraInicioAsc(fecha)).thenReturn(Arrays.asList(reserva));

        // Act
        var result = reservaService.getReservasByFechaInicio(fecha);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(reserva, result.get(0));
    }

    @Test
    void testSave() {
        // Arrange
        when(reservaRepository.save(reserva)).thenReturn(reserva);

        // Act
        Reserva result = reservaService.save(reserva);

        // Assert
        assertNotNull(result);
        assertEquals(reserva, result);
        verify(reservaRepository, times(1)).save(reserva);
    }

    @Test
    void testDeleteById() {
        // Act
        reservaService.deleteById(1L);

        // Assert
        verify(reservaRepository, times(1)).deleteById(1L);
    }

    @Test
    void testUpdate_Found() {
        // Arrange
        Reserva updatedReserva = new Reserva(1L, null, null, 6, 4, 250, 130, LocalDate.now().atTime(11, 0), "Nuevo Cliente", LocalDate.now(), LocalTime.of(11, 0), LocalTime.of(13, 0));
        when(reservaRepository.findById(1L)).thenReturn(Optional.of(reserva));
        when(reservaRepository.save(any(Reserva.class))).thenReturn(updatedReserva);

        // Act
        Reserva result = reservaService.update(1L, updatedReserva);

        // Assert
        assertNotNull(result);
        assertEquals("Nuevo Cliente", result.getNombreCliente());
        assertEquals(250, result.getPrecio_regular());
    }

    @Test
    void testUpdate_NotFound() {
        // Arrange
        Reserva updatedReserva = new Reserva(1L, null, null, 6, 4, 250, 130, LocalDate.now().atTime(11, 0), "Nuevo Cliente", LocalDate.now(), LocalTime.of(11, 0), LocalTime.of(13, 0));
        when(reservaRepository.findById(1L)).thenReturn(Optional.empty());

        // Act
        Reserva result = reservaService.update(1L, updatedReserva);

        // Assert
        assertNull(result);
    }

    @Test
    void testObtenerReservaPorFechaHoraInicioYHoraFin_Found() {
        // Arrange
        LocalDate fecha = LocalDate.now();
        LocalTime horaInicio = LocalTime.of(10, 0);
        LocalTime horaFin = LocalTime.of(12, 0);
        when(reservaRepository.findByFechaInicioAndHoraInicioAndHoraFin(fecha, horaInicio, horaFin)).thenReturn(Optional.of(reserva));

        // Act
        Optional<Reserva> result = reservaService.obtenerReservaPorFechaHoraInicioYHoraFin(fecha, horaInicio, horaFin);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(reserva, result.get());
    }

    @Test
    void testObtenerReservaPorFechaHoraInicioYHoraFin_NotFound() {
        // Arrange
        LocalDate fecha = LocalDate.now();
        LocalTime horaInicio = LocalTime.of(10, 0);
        LocalTime horaFin = LocalTime.of(12, 0);
        when(reservaRepository.findByFechaInicioAndHoraInicioAndHoraFin(fecha, horaInicio, horaFin)).thenReturn(Optional.empty());

        // Act
        Optional<Reserva> result = reservaService.obtenerReservaPorFechaHoraInicioYHoraFin(fecha, horaInicio, horaFin);

        // Assert
        assertFalse(result.isPresent());
    }

    //------------------ ASIGNAR DESCUENTO ----------------- //

    @Test
    void testAsignarPrecioRegular_DuracionTotal_TodasLasRamas() {
        ReservaService reservaService = new ReservaService();

        // ----- Caso 1: Vueltas = 10, día normal (no fin de semana ni feriado) -----
        Reserva reserva1 = new Reserva();
        reserva1.setNum_vueltas_tiempo_maximo(10);
        reserva1.setFechaInicio(LocalDate.of(2025, 3, 4)); // martes, no feriado

        reservaService.asignarPrecioRegular_DuracionTotal(reserva1);
        assertEquals(15000, reserva1.getPrecio_regular());
        assertEquals(30, reserva1.getDuracion_total());

        // ----- Caso 2: Vueltas = 15, fin de semana -----
        Reserva reserva2 = new Reserva();
        reserva2.setNum_vueltas_tiempo_maximo(15);
        reserva2.setFechaInicio(LocalDate.of(2025, 3, 8)); // sábado

        reservaService.asignarPrecioRegular_DuracionTotal(reserva2);
        assertEquals(23000, reserva2.getPrecio_regular()); // 20000 * 1.15
        assertEquals(35, reserva2.getDuracion_total());

        // ----- Caso 3: Vueltas = 20, feriado -----
        Reserva reserva3 = new Reserva();
        reserva3.setNum_vueltas_tiempo_maximo(20);
        reserva3.setFechaInicio(LocalDate.of(2025, 12, 25)); // feriado

        reservaService.asignarPrecioRegular_DuracionTotal(reserva3);
        assertEquals(28750, reserva3.getPrecio_regular()); // 25000 * 1.15
        assertEquals(40, reserva3.getDuracion_total());

        // ----- Caso 4: Vueltas = 5, día normal (ninguna condición) -----
        Reserva reserva4 = new Reserva();
        reserva4.setNum_vueltas_tiempo_maximo(5);
        reserva4.setFechaInicio(LocalDate.of(2025, 3, 5)); // miércoles, no feriado

        reservaService.asignarPrecioRegular_DuracionTotal(reserva4);
        assertEquals(0, reserva4.getPrecio_regular()); // no entra en ningún if
        assertEquals(0, reserva4.getDuracion_total());
    }

    @Test
    void testAsignarPrecioRegular_DuracionTotal_CuandoEsSabado() {
        // Arrange
        reserva.setNum_vueltas_tiempo_maximo(10);
        reserva.setFechaInicio(LocalDate.of(2025, 4, 6)); // Sábado

        // Act
        reservaService.asignarPrecioRegular_DuracionTotal(reserva);

        // Assert
        assertEquals(17250, reserva.getPrecio_regular()); // 15000 * 1.15 = 17250
        assertEquals(30, reserva.getDuracion_total());
    }

    @Test
    void testEsReservaPosible_CuandoNoHayCruce() {
        // Arrange
        LocalDate fecha = LocalDate.of(2025, 4, 25);
        LocalTime horaInicio = LocalTime.of(10, 0);
        LocalTime horaFin = LocalTime.of(11, 0);

        when(reservaRepository.findReservasQueSeCruzan(fecha, horaInicio, horaFin))
                .thenReturn(Collections.emptyList()); // No hay reservas que se crucen

        // Act
        boolean resultado = reservaService.esReservaPosible(fecha, horaInicio, horaFin);

        // Assert
        assertTrue(resultado);
    }

    @Test
    void testEsReservaPosible_CuandoHayCruce() {
        // Arrange
        LocalDate fecha = LocalDate.of(2025, 4, 25);
        LocalTime horaInicio = LocalTime.of(10, 0);
        LocalTime horaFin = LocalTime.of(11, 0);

        Reserva reservaExistente = new Reserva(); // Puede estar vacío, es solo para test
        when(reservaRepository.findReservasQueSeCruzan(fecha, horaInicio, horaFin))
                .thenReturn(List.of(reservaExistente)); // Simula un cruce

        // Act
        boolean resultado = reservaService.esReservaPosible(fecha, horaInicio, horaFin);

        // Assert
        assertFalse(resultado);
    }

    @Test
    void testObtenerInformacionReservaConComprobante() {
        // Crear una reserva de ejemplo
        Reserva reserva = new Reserva();
        Comprobante comprobante = new Comprobante();
        reserva.setId(1L);
        reserva.setFechaHora(LocalDateTime.of(2025, 5, 1, 10, 0));
        reserva.setFechaInicio(LocalDate.of(2025, 5, 1));
        reserva.setHoraInicio(LocalTime.of(10, 0));
        reserva.setHoraFin(LocalTime.of(10, 20));
        reserva.setNum_vueltas_tiempo_maximo(5);
        reserva.setNum_personas(1);
        reserva.setNombreCliente("Cliente Único");

        // Asignar directamente el comprobante a la reserva
        reserva.setComprobante(comprobante);

        // Mockear el método que formatea el comprobante
        when(comprobanteService.formatearComprobante(comprobante)).thenReturn("Precio total: 10000");

        // Mockear el guardado (opcional si no se llama en este método)
        when(reservaRepository.save(reserva)).thenReturn(reserva);

        // Act
        String info = reservaService.obtenerInformacionReservaConComprobante(reserva);

        // Assert
        assertNotNull(info);
        assertTrue(info.contains("Código de la reserva: 1"));
        assertTrue(info.contains("Cliente Único"));
        assertTrue(info.contains("10000")); // Esta línea ahora sí debería pasar
    }

    @Test
    void testObtenerTodosLosHorariosOcupados() {
        // Arrange
        Reserva reserva1 = new Reserva();
        reserva1.setFechaInicio(LocalDate.of(2025, 5, 10));
        reserva1.setHoraInicio(LocalTime.of(10, 0));
        reserva1.setHoraFin(LocalTime.of(10, 30));

        Reserva reserva2 = new Reserva();
        reserva2.setFechaInicio(LocalDate.of(2025, 5, 10));
        reserva2.setHoraInicio(LocalTime.of(11, 0));
        reserva2.setHoraFin(LocalTime.of(11, 45));

        Reserva reserva3 = new Reserva();
        reserva3.setFechaInicio(LocalDate.of(2025, 5, 11));
        reserva3.setHoraInicio(LocalTime.of(9, 0));
        reserva3.setHoraFin(LocalTime.of(9, 15));

        List<Reserva> todasLasReservas = List.of(reserva1, reserva2, reserva3);

        when(reservaRepository.findAll()).thenReturn(todasLasReservas);

        // Act
        Map<LocalDate, List<String>> resultado = reservaService.obtenerTodosLosHorariosOcupados();

        // Assert
        assertEquals(2, resultado.size()); // 2 días distintos

        // Día 10 de mayo
        List<String> horariosDia10 = resultado.get(LocalDate.of(2025, 5, 10));
        assertNotNull(horariosDia10);
        assertEquals(2, horariosDia10.size());
        assertTrue(horariosDia10.contains("10:00 - 10:30"));
        assertTrue(horariosDia10.contains("11:00 - 11:45"));

        // Día 11 de mayo
        List<String> horariosDia11 = resultado.get(LocalDate.of(2025, 5, 11));
        assertNotNull(horariosDia11);
        assertEquals(1, horariosDia11.size());
        assertEquals("09:00 - 09:15", horariosDia11.get(0));
    }

    @Test
    void testObtenerTodosLosHorariosOcupados_SinReservas() {
        // Arrange
        when(reservaRepository.findAll()).thenReturn(List.of());

        // Act
        Map<LocalDate, List<String>> resultado = reservaService.obtenerTodosLosHorariosOcupados();

        // Assert
        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
    }


    @Test
    void testSinReservasDevuelveHorariosCompletos() {
        // Dado
        LocalDate fechaReferencia = LocalDate.of(2025, 5, 1); // Mayo 2025
        when(reservaRepository.findByFechaInicioOrderByHoraInicioAsc(any()))
                .thenReturn(new ArrayList<>()); // No hay reservas en ningún día

        // Cuando
        Map<LocalDate, List<String>> resultado = reservaService.obtenerHorariosDisponiblesProximosSeisMeses(fechaReferencia);

        // Entonces: por ejemplo, validamos el primer día
        LocalDate primerDia = fechaReferencia.withDayOfMonth(1);
        List<String> horariosDelPrimerDia = resultado.get(primerDia);

        // Si el 1 de mayo 2025 es día de semana, el horario debe ser 14:00 - 22:00
        // Si es fin de semana o feriado, debe ser 10:00 - 22:00
        String horarioEsperado;
        if (reservaService.esFinDeSemana(primerDia) || reservaService.esDiaFeriado(primerDia)) {
            horarioEsperado = "10:00 - 22:00";
        } else {
            horarioEsperado = "14:00 - 22:00";
        }

        assertTrue(horariosDelPrimerDia.contains(horarioEsperado));
    }

    @Test
    void testReservaIntermediaDivideHorario() {
        LocalDate fecha = LocalDate.of(2025, 5, 5); // Lunes (día hábil)

        Reserva reserva = new Reserva();
        reserva.setHoraInicio(LocalTime.of(16, 0));
        reserva.setHoraFin(LocalTime.of(16, 30));

        when(reservaRepository.findByFechaInicioOrderByHoraInicioAsc(eq(fecha)))
                .thenReturn(new ArrayList<>(List.of(reserva)));

        when(reservaRepository.findByFechaInicioOrderByHoraInicioAsc(argThat(d -> !d.equals(fecha))))
                .thenReturn(new ArrayList<>());

        Map<LocalDate, List<String>> resultado = reservaService.obtenerHorariosDisponiblesProximosSeisMeses(fecha);
        List<String> horarios = resultado.get(fecha);

        assertTrue(horarios.contains("14:00 - 16:00"), "Debe haber horario libre antes de la reserva");
        assertTrue(horarios.contains("16:30 - 22:00"), "Debe haber horario libre después de la reserva");
    }

    @Test
    void testGenerarReporteIngresosPorVueltas() {
        LocalDate fechaInicio = LocalDate.of(2025, 1, 1);
        LocalDate fechaFin = LocalDate.of(2025, 1, 31);

        Reserva reserva1 = new Reserva();
        reserva1.setNum_vueltas_tiempo_maximo(10);
        Comprobante comp1 = new Comprobante();
        comp1.setMonto_total_iva(100.0);
        reserva1.setComprobante(comp1);

        Reserva reserva2 = new Reserva();
        reserva2.setNum_vueltas_tiempo_maximo(15);
        Comprobante comp2 = new Comprobante();
        comp2.setMonto_total_iva(150.0);
        reserva2.setComprobante(comp2);

        Reserva reserva3 = new Reserva();
        reserva3.setNum_vueltas_tiempo_maximo(20);
        Comprobante comp3 = new Comprobante();
        comp3.setMonto_total_iva(200.0);
        reserva3.setComprobante(comp3);

        Reserva reserva4 = new Reserva();
        reserva4.setNum_vueltas_tiempo_maximo(5);
        Comprobante comp4 = new Comprobante();
        comp4.setMonto_total_iva(50.0);
        reserva4.setComprobante(comp4);

        List<Reserva> reservas = List.of(reserva1, reserva2, reserva3, reserva4);
        Map<String, List<Reserva>> agrupadas = Map.of("2025-01", reservas);

        ReservaService spyService = Mockito.spy(new ReservaService());
        Mockito.doReturn(reservas).when(spyService).obtenerReservasPorRangoDeMeses(fechaInicio, fechaFin);
        Mockito.doReturn(agrupadas).when(spyService).agruparReservasPorMesYAnio(reservas);

        Map<String, Map<String, Double>> reporte = spyService.generarReporteIngresosPorVueltas(fechaInicio, fechaFin);

        assertEquals(1, reporte.size());
        Map<String, Double> ingresos = reporte.get("2025-01");

        assertEquals(100.0, ingresos.get("10"));
        assertEquals(150.0, ingresos.get("15"));
        assertEquals(200.0, ingresos.get("20"));
        assertEquals(500.0, ingresos.get("TOTAL"));
    }

    @Test
    void testGenerarReporteIngresosPorGrupoDePersonas() {
        LocalDate fechaInicio = LocalDate.of(2025, 1, 1);
        LocalDate fechaFin = LocalDate.of(2025, 1, 31);

        // Reserva para grupo 1-2
        Reserva r1 = new Reserva();
        r1.setNum_personas(2);
        Comprobante c1 = new Comprobante();
        c1.setMonto_total_iva(100.0);
        r1.setComprobante(c1);

        // Reserva para grupo 3-5
        Reserva r2 = new Reserva();
        r2.setNum_personas(4);
        Comprobante c2 = new Comprobante();
        c2.setMonto_total_iva(150.0);
        r2.setComprobante(c2);

        // Reserva para grupo 6-10
        Reserva r3 = new Reserva();
        r3.setNum_personas(7);
        Comprobante c3 = new Comprobante();
        c3.setMonto_total_iva(200.0);
        r3.setComprobante(c3);

        // Reserva para grupo 11-15
        Reserva r4 = new Reserva();
        r4.setNum_personas(12);
        Comprobante c4 = new Comprobante();
        c4.setMonto_total_iva(250.0);
        r4.setComprobante(c4);

        // Reserva sin comprobante (debe ignorarse)
        Reserva r5 = new Reserva();
        r5.setNum_personas(3);
        r5.setComprobante(null);

        List<Reserva> reservas = List.of(r1, r2, r3, r4, r5);
        Map<String, List<Reserva>> agrupadas = Map.of("2025-01", reservas);

        ReservaService spyService = Mockito.spy(new ReservaService());
        Mockito.doReturn(reservas).when(spyService).obtenerReservasPorRangoDeMeses(fechaInicio, fechaFin);
        Mockito.doReturn(agrupadas).when(spyService).agruparReservasPorMesYAnio(reservas);

        Map<String, Map<String, Double>> reporte = spyService.generarReporteIngresosPorGrupoDePersonas(fechaInicio, fechaFin);

        assertEquals(1, reporte.size());
        Map<String, Double> ingresos = reporte.get("2025-01");

        assertEquals(100.0, ingresos.get("1-2"));
        assertEquals(150.0, ingresos.get("3-5"));
        assertEquals(200.0, ingresos.get("6-10"));
        assertEquals(250.0, ingresos.get("11-15"));
        assertEquals(700.0, ingresos.get("TOTAL"));
    }





    @Test
    void testAgruparReservasPorMesYAnio() {
        // Creamos varias reservas con fechas diferentes
        Reserva reserva1 = new Reserva();
        reserva1.setFechaInicio(LocalDate.of(2025, 5, 15)); // Mayo 2025

        Reserva reserva2 = new Reserva();
        reserva2.setFechaInicio(LocalDate.of(2025, 5, 20)); // Mayo 2025

        Reserva reserva3 = new Reserva();
        reserva3.setFechaInicio(LocalDate.of(2025, 6, 10)); // Junio 2025

        Reserva reserva4 = new Reserva();
        reserva4.setFechaInicio(LocalDate.of(2025, 6, 25)); // Junio 2025

        Reserva reserva5 = new Reserva();
        reserva5.setFechaInicio(LocalDate.of(2025, 7, 5)); // Julio 2025

        List<Reserva> reservas = List.of(reserva1, reserva2, reserva3, reserva4, reserva5);

        // Ejecutamos el método
        Map<String, List<Reserva>> resultado = reservaService.agruparReservasPorMesYAnio(reservas);

        // Verificamos que las claves sean correctas
        assertTrue(resultado.containsKey("2025-05"), "Debe haber un grupo para mayo 2025");
        assertTrue(resultado.containsKey("2025-06"), "Debe haber un grupo para junio 2025");
        assertTrue(resultado.containsKey("2025-07"), "Debe haber un grupo para julio 2025");

        // Verificamos el tamaño de cada grupo
        assertEquals(2, resultado.get("2025-05").size(), "Mayo debe tener 2 reservas");
        assertEquals(2, resultado.get("2025-06").size(), "Junio debe tener 2 reservas");
        assertEquals(1, resultado.get("2025-07").size(), "Julio debe tener 1 reserva");

        // Verificamos que el orden sea el correcto (ordenado por clave año-mes)
        List<String> keys = new ArrayList<>(resultado.keySet());
        assertEquals("2025-05", keys.get(0), "El primer grupo debe ser mayo 2025");
        assertEquals("2025-06", keys.get(1), "El segundo grupo debe ser junio 2025");
        assertEquals("2025-07", keys.get(2), "El tercer grupo debe ser julio 2025");
    }

    @Test
    public void testObtenerReservasPorRangoDeMeses_validas() {
        // Definir el rango de fechas
        LocalDate fechaInicio = LocalDate.of(2025, 5, 1);
        LocalDate fechaFin = LocalDate.of(2025, 5, 31);

        // Simular el comportamiento del repositorio
        when(reservaRepository.findByFechaInicioBetween(fechaInicio.withDayOfMonth(1), fechaFin.withDayOfMonth(fechaFin.lengthOfMonth())))
                .thenReturn(List.of(reserva));

        // Llamar al método del servicio
        List<Reserva> reservas = reservaService.obtenerReservasPorRangoDeMeses(fechaInicio, fechaFin);

        // Verificar que la lista de reservas no esté vacía
        assertNotNull(reservas);
        assertEquals(1, reservas.size());
        assertEquals(reserva, reservas.get(0));

        // Verificar que el repositorio haya sido llamado correctamente
        verify(reservaRepository, times(1))
                .findByFechaInicioBetween(fechaInicio.withDayOfMonth(1), fechaFin.withDayOfMonth(fechaFin.lengthOfMonth()));
    }

    @Test
    public void testObtenerReservasPorRangoDeMeses_fechaInicioPosteriorAFin() {
        // Definir fechas con fecha de inicio posterior a la fecha de fin
        LocalDate fechaInicio = LocalDate.of(2025, 6, 1);
        LocalDate fechaFin = LocalDate.of(2025, 5, 31);

        // Verificar que el servicio lanza una excepción cuando la fecha de inicio es posterior a la de fin
        assertThrows(IllegalArgumentException.class, () -> {
            reservaService.obtenerReservasPorRangoDeMeses(fechaInicio, fechaFin);
        });
    }

    @Test
    public void testObtenerReservasPorRangoDeMeses_rangoVacio() {
        // Definir un rango de fechas que no debería retornar resultados
        LocalDate fechaInicio = LocalDate.of(2025, 4, 1);
        LocalDate fechaFin = LocalDate.of(2025, 4, 30);

        // Simular que el repositorio no devuelve resultados
        when(reservaRepository.findByFechaInicioBetween(fechaInicio.withDayOfMonth(1), fechaFin.withDayOfMonth(fechaFin.lengthOfMonth())))
                .thenReturn(List.of());

        // Llamar al método del servicio
        List<Reserva> reservas = reservaService.obtenerReservasPorRangoDeMeses(fechaInicio, fechaFin);

        // Verificar que la lista de reservas esté vacía
        assertNotNull(reservas);
        assertTrue(reservas.isEmpty());

        // Verificar que el repositorio haya sido llamado correctamente
        verify(reservaRepository, times(1))
                .findByFechaInicioBetween(fechaInicio.withDayOfMonth(1), fechaFin.withDayOfMonth(fechaFin.lengthOfMonth()));
    }

    @Test
    void testCrearReserva() {
        // Mocks necesarios
        List<Kart> kartsMock = List.of(new Kart(1L, "Modelo A", "KART-001"));
        Mockito.when(kartService.findAll()).thenReturn(kartsMock);

        // Simula creación de comprobante si es parte de la lógica
        Comprobante comprobante = new Comprobante();
        comprobante.setId(1L);
        comprobante.setMonto_total_iva(357.0);
        when(comprobanteService.crearComprobante(
                anyInt(),
                anyInt(),
                anyInt(),
                anyString(),
                anyString(),
                anyMap(),
                anyList()
        )).thenReturn(comprobante);


        // Simula que al guardar la reserva se retorna una con ID asignado
        Mockito.when(reservaRepository.save(Mockito.any())).thenAnswer(inv -> {
            Reserva r = inv.getArgument(0);
            r.setId(1L);
            return r;
        });

        // Ahora puedes llamar al método real
        Reserva reserva = reservaService.crearReserva(
                10, 1, List.of("correo@example.com"),
                LocalDate.of(2025, 1, 1),
                LocalTime.of(15, 0),
                1,
                "Juan Pérez",
                "juan@example.com",
                Map.of("Juan Pérez", "juan@example.com")
        );

        // Verificaciones
        assertNotNull(reserva);
        assertEquals(1, reserva.getNum_personas());
        assertEquals(357.0, reserva.getComprobante().getMonto_total_iva());
    }
}
