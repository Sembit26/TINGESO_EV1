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

    private Reserva crearReserva(LocalTime inicio, LocalTime fin) {
        Reserva r = new Reserva();
        r.setHoraInicio(inicio);
        r.setHoraFin(fin);
        return r;
    }


    @Test
    void testObtenerHorariosOcupadosMes() {
        // Fecha cualquiera dentro de mayo 2025
        LocalDate fecha = LocalDate.of(2025, 5, 10);

        // Simular reservas para dos días
        List<Reserva> reservasDia5 = List.of(
                crearReserva(LocalTime.of(10, 0), LocalTime.of(10, 20)),
                crearReserva(LocalTime.of(11, 0), LocalTime.of(11, 30)),
                crearReserva(LocalTime.of(12, 0), LocalTime.of(12, 45))
        );

        List<Reserva> reservasDia10 = List.of(
                crearReserva(LocalTime.of(9, 0), LocalTime.of(9, 15)),
                crearReserva(LocalTime.of(14, 0), LocalTime.of(14, 30))
        );

        // Mockear los días con reservas
        when(reservaRepository.findByFechaInicioOrderByHoraInicioAsc(LocalDate.of(2025, 5, 5)))
                .thenReturn(reservasDia5);
        when(reservaRepository.findByFechaInicioOrderByHoraInicioAsc(LocalDate.of(2025, 5, 10)))
                .thenReturn(reservasDia10);

        // Mockear los demás días con listas vacías
        for (int d = 1; d <= 31; d++) {
            LocalDate dia = LocalDate.of(2025, 5, d);
            if (!dia.equals(LocalDate.of(2025, 5, 5)) && !dia.equals(LocalDate.of(2025, 5, 10))) {
                when(reservaRepository.findByFechaInicioOrderByHoraInicioAsc(dia))
                        .thenReturn(List.of());
            }
        }

        // Act
        Map<LocalDate, List<String>> resultado = reservaService.obtenerHorariosOcupadosMes(fecha);

        // Assert
        assertEquals(31, resultado.size()); // Mayo tiene 31 días
        assertEquals(List.of("10:00 - 10:20", "11:00 - 11:30", "12:00 - 12:45"), resultado.get(LocalDate.of(2025, 5, 5)));
        assertEquals(List.of("09:00 - 09:15", "14:00 - 14:30"), resultado.get(LocalDate.of(2025, 5, 10)));
        assertEquals(List.of(), resultado.get(LocalDate.of(2025, 5, 1))); // Un día sin reservas
    }


    @Test
    void testObtenerHorariosDisponiblesMes() {
        LocalDate fechaReferencia = LocalDate.of(2025, 5, 1);

        // Día 1: sin reservas
        when(reservaRepository.findByFechaInicioOrderByHoraInicioAsc(LocalDate.of(2025, 5, 1)))
                .thenReturn(new ArrayList<>());

        // Día 2: una reserva a mitad de la tarde
        when(reservaRepository.findByFechaInicioOrderByHoraInicioAsc(LocalDate.of(2025, 5, 2)))
                .thenReturn(new ArrayList<>(List.of(
                        crearReserva(LocalTime.of(15, 0), LocalTime.of(15, 30))
                )));

        // Día 3: reservas seguidas
        when(reservaRepository.findByFechaInicioOrderByHoraInicioAsc(LocalDate.of(2025, 5, 3)))
                .thenReturn(new ArrayList<>(List.of(
                        crearReserva(LocalTime.of(14, 0), LocalTime.of(15, 0)),
                        crearReserva(LocalTime.of(15, 0), LocalTime.of(19, 30))
                )));

        // Día 4: reserva al final
        when(reservaRepository.findByFechaInicioOrderByHoraInicioAsc(LocalDate.of(2025, 5, 4)))
                .thenReturn(new ArrayList<>(List.of(
                        crearReserva(LocalTime.of(19, 0), LocalTime.of(20, 0))
                )));

        // Día 5: todo ocupado
        when(reservaRepository.findByFechaInicioOrderByHoraInicioAsc(LocalDate.of(2025, 5, 5)))
                .thenReturn(new ArrayList<>(List.of(
                        crearReserva(LocalTime.of(14, 0), LocalTime.of(20, 0))
                )));

        Map<LocalDate, List<String>> resultado = reservaService.obtenerHorariosDisponiblesMes(fechaReferencia);

        // Verificar horarios libres para cada día

        // Día 1: Todo el día libre de 14:00 a 20:00
        assertTrue(resultado.get(LocalDate.of(2025, 5, 1)).contains("14:00 - 20:00"));

        // Día 2: Horarios disponibles antes y después de la reserva (de 14:00 a 15:00 y de 15:30 a 20:00)
        assertTrue(resultado.get(LocalDate.of(2025, 5, 2)).contains("14:00 - 15:00"));
        assertTrue(resultado.get(LocalDate.of(2025, 5, 2)).contains("15:30 - 20:00"));

        // Día 3: Horario disponible al final (de 19:30 a 20:00)
        assertTrue(resultado.get(LocalDate.of(2025, 5, 3)).contains("19:30 - 20:00"));

        // Día 4: Horario disponible antes de la reserva (de 14:00 a 19:00)
        assertTrue(resultado.get(LocalDate.of(2025, 5, 4)).contains("14:00 - 19:00"));

        // Día 5: Sin horarios libres (todo el día ocupado)
        assertTrue(resultado.get(LocalDate.of(2025, 5, 5)).isEmpty());

        // Validar el caso de 30 minutos de duración libre (si horaLibreActual es antes de inicioReserva)
        // Esto se probaría especialmente cuando se comparan los tiempos libres con la reserva que ocurre después.
        List<String> horariosDia = resultado.get(LocalDate.of(2025, 5, 2));
        // Si se agregaron correctamente los intervalos antes de la reserva (14:00 - 15:00) y después de la reserva (15:30 - 20:00)
        assertTrue(horariosDia.contains("14:00 - 15:00"));
        assertTrue(horariosDia.contains("15:30 - 20:00"));
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

























}
