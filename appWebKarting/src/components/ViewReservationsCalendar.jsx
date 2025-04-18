import React, { useEffect, useState } from "react";
import { Calendar, dateFnsLocalizer } from "react-big-calendar";
import { parse, startOfWeek, format, getDay, isBefore } from "date-fns";
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
  const [numVueltasTiempoMaximo, setNumVueltasTiempoMaximo] = useState(10);
  const [horaInicio, setHoraInicio] = useState('');
  const navigate = useNavigate();

  useEffect(() => {
    const cliente = sessionStorage.getItem('cliente');
    if (!cliente) {
      navigate('/login');
      return;
    }

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

            const start = new Date(anio, mes - 1, dia, hIni, mIni);
            const end = new Date(anio, mes - 1, dia, hFin, mFin);

            if (!isBefore(start, new Date())) {
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
