package com.example.demo.Services;

import com.example.demo.Entities.Comprobante;
import com.example.demo.Entities.Kart;
import com.example.demo.Entities.Reserva;
import com.example.demo.Repositories.KartRepository;
import com.example.demo.Repositories.ReservaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

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

    public Optional<Reserva> obtenerReservaPorFechaHoraInicioYHoraFin(LocalDate fechaInicio, LocalTime horaInicio, LocalTime horaFin) {
        // Llamada al repositorio para buscar la reserva
        return reservaRepository.findByFechaInicioAndHoraInicioAndHoraFin(fechaInicio, horaInicio, horaFin);
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

        // Validar hora de inicio permitida (entre 14:00 y 17:20 inclusive)
        LocalTime horaMinima = LocalTime.of(14, 0);
        LocalTime horaMaxima = LocalTime.of(19, 20);

        if (horaInicio.isBefore(horaMinima) || horaInicio.isAfter(horaMaxima)) {
            throw new RuntimeException("La hora de inicio debe estar entre las 14:00 y las 17:20.");
        }

        Reserva reserva = new Reserva();
        reserva.setNum_vueltas_tiempo_maximo(numVueltasTiempoMaximo);
        reserva.setNum_personas(numPersonas);
        reserva.setFechaHora(LocalDateTime.now());
        reserva.setFechaInicio(fechaInicio);
        reserva.setHoraInicio(horaInicio);
        reserva.setNombreCliente(nombreCliente); //ULTIMO QUE SE AGREGO

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

        // Horario fijo del kartódromo
        LocalTime horaInicioDia = LocalTime.of(14, 0);
        LocalTime horaFinDia = LocalTime.of(20, 0);

        // Para cada día de la semana (desde el inicioSemana)
        for (int i = 0; i < 7; i++) {
            LocalDate fecha = inicioSemana.plusDays(i);
            List<Reserva> reservas = getReservasByFechaInicio(fecha);
            List<String> horariosLibres = new ArrayList<>();

            LocalTime horaLibreActual = horaInicioDia;

            // Ordenar reservas por horaInicio para asegurar procesamiento correcto
            reservas.sort(Comparator.comparing(Reserva::getHoraInicio));

            for (Reserva reserva : reservas) {
                LocalTime inicioReserva = reserva.getHoraInicio();
                LocalTime finReserva = reserva.getHoraFin();

                // Verificar si hay un intervalo libre antes de esta reserva
                if (horaLibreActual.isBefore(inicioReserva)) {
                    Duration duracionLibre = Duration.between(horaLibreActual, inicioReserva);
                    if (duracionLibre.toMinutes() >= 30) {
                        horariosLibres.add(horaLibreActual + " - " + inicioReserva);
                    }
                }

                // Avanzar la hora libre actual si la reserva la cubre
                if (horaLibreActual.isBefore(finReserva)) {
                    horaLibreActual = finReserva;
                }
            }

            // Verificar si hay espacio libre después de la última reserva hasta el cierre
            if (horaLibreActual.isBefore(horaFinDia)) {
                Duration duracionLibre = Duration.between(horaLibreActual, horaFinDia);
                if (duracionLibre.toMinutes() >= 30) {
                    horariosLibres.add(horaLibreActual + " - " + horaFinDia);
                }
            }

            horariosDisponiblesSemana.put(fecha, horariosLibres);
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

    // ======================= REPORTES DE INGRESOS =======================

    public List<Reserva> obtenerReservasPorRangoDeMeses(LocalDate fechaInicio, LocalDate fechaFin) {
        // Validar que la fecha de inicio sea antes o igual a la fecha de fin
        if (fechaInicio.isAfter(fechaFin)) {
            throw new IllegalArgumentException("La fecha de inicio no puede ser posterior a la fecha de fin.");
        }

        // Ajustar fechas al primer día del mes y al último día del mes
        LocalDate inicioMes = fechaInicio.withDayOfMonth(1);
        LocalDate finMes = fechaFin.withDayOfMonth(fechaFin.lengthOfMonth());

        return reservaRepository.findByFechaInicioBetween(inicioMes, finMes);
    }


    public Map<String, List<Reserva>> agruparReservasPorMesYAnio(List<Reserva> todasLasReservas) {
        // Usamos un LinkedHashMap para mantener el orden de las claves
        return todasLasReservas.stream()
                .collect(Collectors.groupingBy(reserva -> {
                    // Generar un String que combine el año y mes de la reserva
                    return reserva.getFechaInicio().getYear() + "-" + String.format("%02d", reserva.getFechaInicio().getMonthValue());
                }, LinkedHashMap::new, Collectors.toList())) // Usar LinkedHashMap para mantener el orden de inserción
                .entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey()) // Ordenar por la clave (año-mes)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new // Mantener el orden de inserción
                ));
    }

    public Map<String, Map<String, Double>> generarReporteIngresosPorVueltas(LocalDate fechaInicio, LocalDate fechaFin) {
        List<Reserva> reservas = obtenerReservasPorRangoDeMeses(fechaInicio, fechaFin);
        Map<String, List<Reserva>> reservasAgrupadas = agruparReservasPorMesYAnio(reservas);

        // Usamos TreeMap para que los meses estén ordenados
        Map<String, Map<String, Double>> reporte = new TreeMap<>();

        for (Map.Entry<String, List<Reserva>> entrada : reservasAgrupadas.entrySet()) {
            String mesAnio = entrada.getKey();
            List<Reserva> reservasDelMes = entrada.getValue();

            Map<String, Double> ingresosPorVueltas = new HashMap<>();
            ingresosPorVueltas.put("10", 0.0);
            ingresosPorVueltas.put("15", 0.0);
            ingresosPorVueltas.put("20", 0.0);
            ingresosPorVueltas.put("TOTAL", 0.0);

            for (Reserva reserva : reservasDelMes) {
                int vueltas = reserva.getNum_vueltas_tiempo_maximo();
                Comprobante comprobante = reserva.getComprobante(); // Asumiendo que existe el método
                if (comprobante != null) {
                    double monto = Math.round(comprobante.getMonto_total_iva());
                    if (vueltas == 10 || vueltas == 15 || vueltas == 20) {
                        ingresosPorVueltas.put(String.valueOf(vueltas), ingresosPorVueltas.get(String.valueOf(vueltas)) + monto);
                    }
                    ingresosPorVueltas.put("TOTAL", ingresosPorVueltas.get("TOTAL") + monto);
                }
            }

            reporte.put(mesAnio, ingresosPorVueltas);
        }

        return reporte;
    }

    public Map<String, Map<String, Double>> generarReporteIngresosPorGrupoDePersonas(LocalDate fechaInicio, LocalDate fechaFin) {
        List<Reserva> reservas = obtenerReservasPorRangoDeMeses(fechaInicio, fechaFin);
        Map<String, List<Reserva>> reservasAgrupadas = agruparReservasPorMesYAnio(reservas);

        Map<String, Map<String, Double>> reporte = new TreeMap<>();

        for (Map.Entry<String, List<Reserva>> entrada : reservasAgrupadas.entrySet()) {
            String mesAnio = entrada.getKey();
            List<Reserva> reservasDelMes = entrada.getValue();

            Map<String, Double> ingresosPorGrupo = new LinkedHashMap<>();
            ingresosPorGrupo.put("1-2", 0.0);
            ingresosPorGrupo.put("3-5", 0.0);
            ingresosPorGrupo.put("6-10", 0.0);
            ingresosPorGrupo.put("11-15", 0.0);
            ingresosPorGrupo.put("TOTAL", 0.0);

            for (Reserva reserva : reservasDelMes) {
                int personas = reserva.getNum_personas();
                Comprobante comprobante = reserva.getComprobante();

                if (comprobante != null) {
                    double monto = Math.round(comprobante.getMonto_total_iva());

                    if (personas >= 1 && personas <= 2) {
                        ingresosPorGrupo.put("1-2", ingresosPorGrupo.get("1-2") + monto);
                    } else if (personas >= 3 && personas <= 5) {
                        ingresosPorGrupo.put("3-5", ingresosPorGrupo.get("3-5") + monto);
                    } else if (personas >= 6 && personas <= 10) {
                        ingresosPorGrupo.put("6-10", ingresosPorGrupo.get("6-10") + monto);
                    } else if (personas >= 11 && personas <= 15) {
                        ingresosPorGrupo.put("11-15", ingresosPorGrupo.get("11-15") + monto);
                    }

                    ingresosPorGrupo.put("TOTAL", ingresosPorGrupo.get("TOTAL") + monto);
                }
            }

            reporte.put(mesAnio, ingresosPorGrupo);
        }

        return reporte;
    }

}
