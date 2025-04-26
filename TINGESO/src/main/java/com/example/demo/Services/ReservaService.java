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
            reserva.setNombreCliente(updatedReserva.getNombreCliente());
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

        // Validar hora de inicio permitida según el número de vueltas
        LocalTime horaMaxima;

        // Definir la hora máxima según el número de vueltas
        if (numVueltasTiempoMaximo == 10) {
            horaMaxima = LocalTime.of(19, 30);
        } else if (numVueltasTiempoMaximo == 15) {
            horaMaxima = LocalTime.of(19, 25);
        } else if (numVueltasTiempoMaximo == 20) {
            horaMaxima = LocalTime.of(19, 20);
        } else {
            // Si el numVueltasTiempoMaximo no está en los valores esperados, se establece una hora máxima general
            horaMaxima = LocalTime.of(19, 20);
        }

        // Crear la reserva
        Reserva reserva = new Reserva();
        reserva.setNum_vueltas_tiempo_maximo(numVueltasTiempoMaximo);
        reserva.setNum_personas(numPersonas);
        reserva.setFechaHora(LocalDateTime.now());
        reserva.setFechaInicio(fechaInicio);
        reserva.setHoraInicio(horaInicio);
        reserva.setNombreCliente(nombreCliente);

        // Calcular duración y precio
        asignarPrecioRegular_DuracionTotal(reserva);

        // Calcular la hora de fin según la duración
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

        // Guardar la reserva
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
        }

        // Aumentar precio si es fin de semana o feriado
        LocalDate fecha = reserva.getFechaInicio();
        boolean esFinDeSemana = fecha.getDayOfWeek().getValue() == 6 || fecha.getDayOfWeek().getValue() == 7;
        boolean esFeriado = esDiaFeriado(fecha);

        if (esFinDeSemana || esFeriado) {
            precioBase = (int) Math.round(precioBase * 1.15);
        }

        reserva.setPrecio_regular(precioBase);
        reserva.setDuracion_total(duracion);
    }

    public boolean esDiaFeriado(LocalDate fecha) {
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

    public String obtenerInformacionReservaConComprobante(Reserva reserva) {
        Comprobante comprobante = reserva.getComprobante();
        String nombreCliente = reserva.getNombreCliente();

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

    public Map<LocalDate, List<String>> obtenerTodosLosHorariosOcupados() {
        List<Reserva> reservas = reservaRepository.findAll(); // Obtener todas las reservas
        Map<LocalDate, List<String>> horariosOcupados = new HashMap<>();

        for (Reserva reserva : reservas) {
            LocalDate fecha = reserva.getFechaInicio(); // Asumiendo que la entidad tiene una fecha de inicio
            LocalTime horaInicio = reserva.getHoraInicio();
            LocalTime horaFin = reserva.getHoraFin();
            String horario = horaInicio + " - " + horaFin;

            // Agregar el horario a la lista correspondiente a la fecha
            horariosOcupados.computeIfAbsent(fecha, k -> new ArrayList<>()).add(horario);
        }

        return horariosOcupados;
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

    public Map<LocalDate, List<String>> obtenerHorariosDisponiblesProximosSeisMeses(LocalDate fechaCualquieraDelMes) {
        Map<LocalDate, List<String>> horariosDisponiblesMes = new LinkedHashMap<>();

        LocalDate fechaInicio = fechaCualquieraDelMes.withDayOfMonth(1);
        LocalDate fechaFin = fechaInicio.plusMonths(6).withDayOfMonth(
                fechaInicio.plusMonths(6).lengthOfMonth()
        );

        for (LocalDate fecha = fechaInicio; !fecha.isAfter(fechaFin); fecha = fecha.plusDays(1)) {
            // Ajustar el horario de inicio según el tipo de día
            LocalTime horaInicioDia;
            if (esFinDeSemana(fecha) || esDiaFeriado(fecha)) {
                horaInicioDia = LocalTime.of(10, 0);
            } else {
                horaInicioDia = LocalTime.of(14, 0);
            }
            LocalTime horaFinDia = LocalTime.of(22, 0);

            List<Reserva> reservas = getReservasByFechaInicio(fecha);
            List<String> horariosLibres = new ArrayList<>();
            LocalTime horaLibreActual = horaInicioDia;

            reservas.sort(Comparator.comparing(Reserva::getHoraInicio));

            for (Reserva reserva : reservas) {
                LocalTime inicioReserva = reserva.getHoraInicio();
                LocalTime finReserva = reserva.getHoraFin();

                if (horaLibreActual.isBefore(inicioReserva)) {
                    Duration duracionLibre = Duration.between(horaLibreActual, inicioReserva);
                    if (duracionLibre.toMinutes() >= 30) {
                        horariosLibres.add(horaLibreActual + " - " + inicioReserva);
                    }
                }

                if (horaLibreActual.isBefore(finReserva)) {
                    horaLibreActual = finReserva;
                }
            }

            if (horaLibreActual.isBefore(horaFinDia)) {
                Duration duracionLibre = Duration.between(horaLibreActual, horaFinDia);
                if (duracionLibre.toMinutes() >= 30) {
                    horariosLibres.add(horaLibreActual + " - " + horaFinDia);
                }
            }

            horariosDisponiblesMes.put(fecha, horariosLibres);
        }

        return horariosDisponiblesMes;
    }

    public boolean esFinDeSemana(LocalDate fecha) {
        DayOfWeek dia = fecha.getDayOfWeek();
        return dia == DayOfWeek.SATURDAY || dia == DayOfWeek.SUNDAY;
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
