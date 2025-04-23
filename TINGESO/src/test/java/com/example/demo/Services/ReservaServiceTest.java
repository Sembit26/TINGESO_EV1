package com.example.demo.Services;

import com.example.demo.Entities.Comprobante;
import com.example.demo.Entities.Reserva;
import com.example.demo.Repositories.ReservaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ReservaServiceTest {

    @Mock
    private ReservaRepository reservaRepository;

    @InjectMocks
    private ReservaService reservaService;

    @Mock
    private ComprobanteService comprobanteService;

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

















}
