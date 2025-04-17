package com.example.demo.Services;

import com.example.demo.Entities.Comprobante;
import com.example.demo.Entities.Kart;
import com.example.demo.Entities.Reserva;
import com.example.demo.Repositories.KartRepository;
import com.example.demo.Repositories.ReservaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class ReservaService {

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private ComprobanteService comprobanteService;

    @Autowired
    private KartService kartService;

    // ======================= OPERACIONES CRUD =======================

    public List<Reserva> findAll() {
        return reservaRepository.findAll();
    }

    public Optional<Reserva> findById(Long id) {
        return reservaRepository.findById(id);
    }

    public List<Reserva> getReservasByFechaInicio(LocalDate fecha) {
        return reservaRepository.findByFechaInicioOrderByHoraInicioAsc(fecha);
    }

    public Reserva save(Reserva reserva) {
        return reservaRepository.save(reserva);
    }

    public void deleteById(Long id) {
        reservaRepository.deleteById(id);
    }

    public Reserva update(Long id, Reserva updatedReserva) {
        return reservaRepository.findById(id).map(reserva -> {
            reserva.setKartsAsignados(updatedReserva.getKartsAsignados());
            reserva.setNum_vueltas_tiempo_maximo(updatedReserva.getNum_vueltas_tiempo_maximo());
            reserva.setNum_personas(updatedReserva.getNum_personas());
            reserva.setPrecio_regular(updatedReserva.getPrecio_regular());
            reserva.setDuracion_total(updatedReserva.getDuracion_total());
            reserva.setFechaHora(updatedReserva.getFechaHora());
            return reservaRepository.save(reserva);
        }).orElse(null);
    }

    // ======================= CREACIÓN DE RESERVA =======================

    public Reserva crearReserva(int numVueltasTiempoMaximo,
                                int numPersonas,
                                List<String> correosCumpleaneros,
                                LocalDate fechaInicio,
                                LocalTime horaInicio,
                                int frecuenciaCliente,
                                String nombreCliente,
                                String correoCliente,
                                Map<String, String> nombreCorreo) {

        Reserva reserva = new Reserva();
        reserva.setNum_vueltas_tiempo_maximo(numVueltasTiempoMaximo);
        reserva.setNum_personas(numPersonas);
        reserva.setFechaHora(LocalDateTime.now());
        reserva.setFechaInicio(fechaInicio);
        reserva.setHoraInicio(horaInicio);

        // Calcular duración y precio
        asignarPrecioRegular_DuracionTotal(reserva);

        LocalTime horaFin = horaInicio.plusMinutes(reserva.getDuracion_total());
        reserva.setHoraFin(horaFin);

        // Verificar disponibilidad
        if (!esReservaPosible(fechaInicio, horaInicio, horaFin)) {
            throw new RuntimeException("Ya existe una reserva en ese horario.");
        }

        // Asignar karts
        List<Kart> kartsDisponibles = kartService.findAll();
        reserva.setKartsAsignados(kartsDisponibles.subList(0, numPersonas));

        // Generar comprobante
        Comprobante comprobante = comprobanteService.crearComprobante(
                reserva.getPrecio_regular(),
                numPersonas,
                frecuenciaCliente,
                nombreCliente,
                correoCliente,
                nombreCorreo,
                correosCumpleaneros
        );
        reserva.setComprobante(comprobante);

        return save(reserva);
    }

    // ======================= LÓGICA DE PRECIO Y DURACIÓN =======================

    public void asignarPrecioRegular_DuracionTotal(Reserva reserva) {
        int precioBase = 0;
        int duracion = 0;

        if (reserva.getNum_vueltas_tiempo_maximo() == 10) {
            precioBase = 15000;
            duracion = 30;
        } else if (reserva.getNum_vueltas_tiempo_maximo() == 15) {
            precioBase = 20000;
            duracion = 35;
        } else if (reserva.getNum_vueltas_tiempo_maximo() == 20) {
            precioBase = 25000;
            duracion = 40;
        } else if (reserva.getNum_vueltas_tiempo_maximo() == 5) {
            precioBase = 10000;
            duracion = 20;
        }

        // Aumentar precio si es fin de semana o feriado
        LocalDate fecha = reserva.getFechaInicio();
        boolean esFinDeSemana = fecha.getDayOfWeek().getValue() == 6 || fecha.getDayOfWeek().getValue() == 7;
        boolean esFeriado = esDiaFeriado(fecha);

        if (esFinDeSemana || esFeriado) {
            precioBase *= 1.15;
        }

        reserva.setPrecio_regular(precioBase);
        reserva.setDuracion_total(duracion);
    }

    private boolean esDiaFeriado(LocalDate fecha) {
        List<LocalDate> feriados = List.of(
                LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 5, 1),
                LocalDate.of(2025, 9, 18),
                LocalDate.of(2025, 12, 25)
        );
        return feriados.contains(fecha);
    }

    // ======================= DISPONIBILIDAD =======================

    public boolean esReservaPosible(LocalDate fecha, LocalTime horaInicio, LocalTime horaFin) {
        List<Reserva> reservasQueSeCruzan = reservaRepository.findReservasQueSeCruzan(fecha, horaInicio, horaFin);
        return reservasQueSeCruzan.isEmpty();
    }

    // ======================= INFORMACIÓN =======================

    public String obtenerInformacionReservaConComprobante(Reserva reserva, String nombreCliente) {
        Comprobante comprobante = reserva.getComprobante();

        DateTimeFormatter formatterFechaHora = DateTimeFormatter.ofPattern("yy/MM/dd HH:mm:ss");
        DateTimeFormatter formatterFecha = DateTimeFormatter.ofPattern("yy/MM/dd");
        DateTimeFormatter formatterHora = DateTimeFormatter.ofPattern("HH:mm:ss");

        StringBuilder informacionReserva = new StringBuilder();
        informacionReserva.append("========= INFORMACIÓN DE LA RESERVA =========\n");
        informacionReserva.append("Código de la reserva: ").append(reserva.getId()).append("\n");
        informacionReserva.append("Fecha y hora de la reserva: ").append(reserva.getFechaHora().format(formatterFechaHora)).append("\n");
        informacionReserva.append("Fecha de inicio: ").append(reserva.getFechaInicio().format(formatterFecha)).append("\n");
        informacionReserva.append("Hora de inicio: ").append(reserva.getHoraInicio().format(formatterHora)).append("\n");
        informacionReserva.append("Hora de fin: ").append(reserva.getHoraFin().format(formatterHora)).append("\n");
        informacionReserva.append("Número de vueltas o tiempo máximo reservado: ").append(reserva.getNum_vueltas_tiempo_maximo()).append("\n");
        informacionReserva.append("Cantidad de personas incluidas: ").append(reserva.getNum_personas()).append("\n");
        informacionReserva.append("Nombre de la persona que hizo la reserva: ").append(nombreCliente.split("\\|")[0]).append("\n");
        informacionReserva.append("\n").append(comprobanteService.formatearComprobante(comprobante));

        return informacionReserva.toString();
    }

    // ======================= HORARIOS DISPONIBLES =======================

    public Map<LocalDate, List<String>> obtenerHorariosDisponiblesSemana(LocalDate inicioSemana) {
        Map<LocalDate, List<String>> horariosDisponiblesSemana = new LinkedHashMap<>();

        // Definir horarios del kartódromo
        LocalTime horaInicioDia = LocalTime.of(14, 0);
        LocalTime horaFinDia = LocalTime.of(20, 0);

        // Iterar por cada día de la semana (7 días)
        for (int i = 0; i < 7; i++) {
            LocalDate fecha = inicioSemana.plusDays(i);

            // Traer reservas de este día
            List<Reserva> reservas = getReservasByFechaInicio(fecha);
            List<String> libres = new ArrayList<>();

            LocalTime horaLibreActual = horaInicioDia;

            for (Reserva reserva : reservas) {
                LocalTime inicioReserva = reserva.getHoraInicio();
                LocalTime finReserva = reserva.getHoraFin();

                // Si hay un intervalo libre antes de la reserva
                if (horaLibreActual.isBefore(inicioReserva)) {
                    libres.add(horaLibreActual.toString() + " - " + inicioReserva.toString());
                }

                // Actualizar la hora libre actual, que será el fin de la reserva
                if (horaLibreActual.isBefore(finReserva)) {
                    horaLibreActual = finReserva;
                }
            }

            // Si hay un intervalo libre después de la última reserva
            if (horaLibreActual.isBefore(horaFinDia)) {
                libres.add(horaLibreActual.toString() + " - " + horaFinDia.toString());
            }

            horariosDisponiblesSemana.put(fecha, libres);
        }

        return horariosDisponiblesSemana;
    }

    public Map<LocalDate, List<String>> obtenerHorariosOcupadosSemana(LocalDate inicioSemana) {
        Map<LocalDate, List<String>> horariosOcupados = new HashMap<>();
        for (int i = 0; i < 7; i++) {
            LocalDate dia = inicioSemana.plusDays(i); // Obtener cada día de la semana
            List<Reserva> reservasDelDia = reservaRepository.findByFechaInicioOrderByHoraInicioAsc(dia); // Obtener reservas de ese día
            List<String> horarios = new ArrayList<>();

            // Recorrer las reservas y extraer los horarios ocupados
            for (Reserva reserva : reservasDelDia) {
                LocalTime horaInicio = reserva.getHoraInicio();
                LocalTime horaFin = reserva.getHoraFin();

                // Formatear los horarios ocupados y añadirlos a la lista
                horarios.add(horaInicio.toString() + " - " + horaFin.toString());
            }
            horariosOcupados.put(dia, horarios); // Añadir los horarios ocupados para ese día
        }
        return horariosOcupados;
    }

    public LocalDate calcularInicioSemana(LocalDate fechaActual) {
        // Calcular el inicio de la semana (lunes) a partir de la fecha actual
        // LocalDate.now().getDayOfWeek() devuelve el día de la semana (por ejemplo, MONDAY, TUESDAY, etc.)
        int diaDeLaSemana = fechaActual.getDayOfWeek().getValue(); // 1 = Lunes, 7 = Domingo

        // Restar la cantidad de días desde el lunes (si es lunes, se resta 0 días)
        return fechaActual.minusDays(diaDeLaSemana - 1);
    }

}
