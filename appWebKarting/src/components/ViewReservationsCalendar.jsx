import React, { useEffect, useState } from "react";
import { Calendar, dateFnsLocalizer } from "react-big-calendar";
import { parse, startOfWeek, format, getDay } from "date-fns";
import 'react-big-calendar/lib/css/react-big-calendar.css';
import reservaService from "../services/reservation.service";
import { useNavigate } from "react-router-dom";
import es from 'date-fns/locale/es';

const locales = { es };

const localizer = dateFnsLocalizer({
  format,
  parse,
  startOfWeek: () => startOfWeek(new Date(), { weekStartsOn: 1 }),
  getDay,
  locales,
});

const ViewReservationsCalendar = () => {
  const [eventos, setEventos] = useState([]);
  const navigate = useNavigate();

  useEffect(() => {
    const cliente = sessionStorage.getItem('cliente');
    const now = new Date();
    now.setSeconds(0);
    now.setMilliseconds(0);

    reservaService.horariosDisponiblesSemana()
      .then((res) => {
        const data = res.data;
        const eventosGenerados = [];

        Object.entries(data).forEach(([fecha, bloques]) => {
          bloques.forEach(bloque => {
            const [inicioStr, finStr] = bloque.split(' - ');
            const [anio, mes, dia] = fecha.split('-').map(Number);
            const [hIni, mIni] = inicioStr.split(':').map(Number);
            const [hFin, mFin] = finStr.split(':').map(Number);

            let start = new Date(anio, mes - 1, dia, hIni, mIni);
            const end = new Date(anio, mes - 1, dia, hFin, mFin);

            const esHoy = start.toDateString() === now.toDateString();

            if (esHoy) {
              // Ajustar el inicio si la hora actual es posterior
              if (start <= now) {
                start = new Date(now); // Clonar "now"
              }
            }

            const minutosDisponibles = (end.getTime() - start.getTime()) / 60000;

            // Agregar evento solo si hay al menos 30 minutos disponibles
            if (end > now && minutosDisponibles >= 30) {
              eventosGenerados.push({
                title: 'Disponible',
                start,
                end,
                allDay: false,
              });
            }
          });
        });

        setEventos(eventosGenerados);
      })
      .catch((err) => {
        console.error("Error al obtener horarios:", err);
      });
  }, [navigate]);

  return (
    <div className="calendar-page">
      <h2 className="calendar-title">Horarios Disponibles de la Semana</h2>

      <div className="calendar-wrapper">
        <Calendar
          localizer={localizer}
          events={eventos}
          startAccessor="start"
          endAccessor="end"
          views={['week']}
          defaultView="week"
          step={20}
          timeslots={2}
          min={new Date(1970, 1, 1, 14, 0)}
          max={new Date(1970, 1, 1, 20, 0)}
          style={{ height: '75vh', width: '100%' }}
          onSelectEvent={(event) => {
            const fecha = format(event.start, 'yyyy-MM-dd');
            const hora = format(event.start, 'HH:mm');
            navigate('/generarReserva', {
              state: { fecha, hora },
            });
          }}
          messages={{
            week: 'Semana',
            day: 'Día',
            today: 'Hoy',
            previous: 'Anterior',
            next: 'Siguiente',
          }}
        />
      </div>
    </div>
  );
};

export default ViewReservationsCalendar;
