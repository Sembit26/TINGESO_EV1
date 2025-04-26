import React, { useEffect, useState } from "react";
import { Calendar, dateFnsLocalizer } from "react-big-calendar";
import { parse, startOfWeek, format, getDay } from "date-fns";
import 'react-big-calendar/lib/css/react-big-calendar.css';
import reservaService from "../services/reservation.service";
import { useNavigate } from "react-router-dom";
import es from 'date-fns/locale/es';
import HamburgerMenu from "../components/HamburgerMenu"; // ✅ Importar menú

const locales = { es };

const localizer = dateFnsLocalizer({
  format,
  parse,
  startOfWeek: () => startOfWeek(new Date(), { weekStartsOn: 1 }),
  getDay,
  locales,
});

const ViewAllReservations = () => {
  const [eventos, setEventos] = useState([]);
  const [currentDate, setCurrentDate] = useState(new Date());
  const navigate = useNavigate();

  const handleEventClick = (event) => {
    const fecha = event.start.toISOString().split('T')[0];
    const horaInicio = event.start.toTimeString().split(' ')[0].slice(0, 5);
    const horaFin = event.end.toTimeString().split(' ')[0].slice(0, 5);
    const reservaId = event.id;

    if (event.tipo === 'disponible') {
      navigate('/generarReservaEmpleado', {
        state: { fecha, hora: horaInicio }
      });
    } else if (event.tipo === 'reservado') {
      navigate('/editarReserva', {
        state: { id: reservaId, fecha, horaInicio, horaFin }
      });
    }
  };

  useEffect(() => {
    const now = new Date();
    now.setSeconds(0);
    now.setMilliseconds(0);

    Promise.all([
      reservaService.horariosDisponiblesSeisMeses(),
      reservaService.obtenerTodasLasReservasOcupadas(),
    ])
      .then(async ([disponiblesRes, ocupadosRes]) => {
        const disponiblesData = disponiblesRes.data;
        const ocupadosData = ocupadosRes.data;
        const nuevosEventos = [];

        Object.entries(disponiblesData).forEach(([fecha, bloques]) => {
          const [anio, mes, dia] = fecha.split('-').map(Number);

          bloques.forEach(bloque => {
            const [inicioStr, finStr] = bloque.split(' - ');
            const [hIni, mIni] = inicioStr.split(':').map(Number);
            const [hFin, mFin] = finStr.split(':').map(Number);

            let start = new Date(anio, mes - 1, dia, hIni, mIni);
            const end = new Date(anio, mes - 1, dia, hFin, mFin);

            const esHoy = start.toDateString() === now.toDateString();
            if (esHoy && start <= now) start = new Date(now);

            const minutosDisponibles = (end.getTime() - start.getTime()) / 60000;
            if (end > now && minutosDisponibles >= 30) {
              nuevosEventos.push({
                title: 'Disponible',
                start,
                end,
                allDay: false,
                tipo: 'disponible',
                id: `${anio}-${mes}-${dia}-${hIni}-${mIni}`
              });
            }
          });
        });

        const promises = [];

        Object.entries(ocupadosData).forEach(([fecha, bloques]) => {
          const [anio, mes, dia] = fecha.split('-').map(Number);

          bloques.forEach(bloque => {
            const [inicioStr, finStr] = bloque.split(' - ');
            const [hIni, mIni] = inicioStr.split(':').map(Number);
            const [hFin, mFin] = finStr.split(':').map(Number);

            const start = new Date(anio, mes - 1, dia, hIni, mIni);
            const end = new Date(anio, mes - 1, dia, hFin, mFin);
            const fechaISO = `${anio}-${String(mes).padStart(2, '0')}-${String(dia).padStart(2, '0')}`;

            const prom = reservaService
              .obtenerReservaPorFechaYHora(fechaISO, inicioStr, finStr)
              .then(res => {
                const nombre = res.data?.nombreCliente || 'Reservado';
                nuevosEventos.push({
                  title: nombre,
                  start,
                  end,
                  allDay: false,
                  tipo: 'reservado',
                  id: res.data.id,
                });
              })
              .catch(() => {
                nuevosEventos.push({
                  title: 'Reservado',
                  start,
                  end,
                  allDay: false,
                  tipo: 'reservado',
                  id: `${anio}-${mes}-${dia}-${hIni}-${mIni}`
                });
              });

            promises.push(prom);
          });
        });

        await Promise.all(promises);
        setEventos(nuevosEventos);
      })
      .catch((err) => {
        console.error("Error al obtener eventos:", err);
      });
  }, [navigate, currentDate]);

  const handleNavigate = (date) => setCurrentDate(date);

  const eventStyleGetter = (event) => {
    let backgroundColor = event.tipo === 'reservado' ? '#81d4fa' : '#2196f3';
    return {
      style: {
        backgroundColor,
        borderRadius: '5px',
        opacity: 0.9,
        color: 'white',
        border: 'none',
        padding: '2px 4px',
      }
    };
  };

  return (
    <div className="calendar-page">
      <HamburgerMenu /> {/* ✅ Nuevo menú hamburguesa */}

      <h2 className="calendar-title">Horarios de la Semana (Disponibles y Reservados)</h2>

      <div className="calendar-wrapper">
        <Calendar
          onSelectEvent={handleEventClick}
          localizer={localizer}
          events={eventos}
          startAccessor="start"
          endAccessor="end"
          views={['week']}
          defaultView="week"
          step={20}
          timeslots={2}
          min={new Date(1970, 1, 1, 10, 0)}
          max={new Date(1970, 1, 1, 22, 0)}
          style={{ height: '75vh', width: '100%' }}
          eventPropGetter={eventStyleGetter}
          messages={{
            week: 'Semana',
            day: 'Día',
            today: 'Hoy',
            previous: 'Anterior',
            next: 'Siguiente',
          }}
          onNavigate={handleNavigate}
          date={currentDate}
        />
      </div>
    </div>
  );
};

export default ViewAllReservations;
