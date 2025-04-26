import httClient from "../http-common";

const RESERVA_API_URL = "/api/reservas"

const horariosDisponiblesSeisMeses = () => {
  return httClient.get(`${RESERVA_API_URL}/horariosDisponiblesSeisMeses`);
};

const obtenerTodasLasReservasOcupadas = () => {
  return httClient.get(`${RESERVA_API_URL}/horariosOcupados`)
}

const obtenerReservaPorFechaYHora = (fechaInicio, horaInicio, horaFin) => {
    return httClient.get(`${RESERVA_API_URL}/obtenerReservaPorFechaYHora`, {
      params: {
        fechaInicio,
        horaInicio,
        horaFin,
      }
    });
};

// Actualizar una reserva por ID
const actualizarReservaPorId = (id, updatedReserva) => {
  return httClient.put(`${RESERVA_API_URL}/updateReservaById/${id}`, updatedReserva);
};

// Obtener información de una reserva por ID
const obtenerInformacionReserva = (id) => {
  return httClient.get(`${RESERVA_API_URL}/getInfoReserva/${id}`);
};

// Eliminar una reserva por ID
const eliminarReservaPorId = (id) => {
  return httClient.delete(`${RESERVA_API_URL}/deleteReservaById/${id}`);
};

// Obtener ingresos por vueltas
const obtenerIngresosPorVueltas = (fechaInicio, fechaFin) => {
  return httClient.get(`${RESERVA_API_URL}/ingresosPorVueltas`, {
    params: { fechaInicio, fechaFin }
  });
};

// Obtener ingresos por cantidad de personas
const obtenerIngresosPorPersonas = (fechaInicio, fechaFin) => {
  return httClient.get(`${RESERVA_API_URL}/ingresosPorPersonas`, {
    params: { fechaInicio, fechaFin }
  });
};

export default {
  obtenerReservaPorFechaYHora,
  horariosDisponiblesSeisMeses,
  actualizarReservaPorId,
  obtenerInformacionReserva,
  eliminarReservaPorId,
  obtenerIngresosPorVueltas,
  obtenerIngresosPorPersonas,
  obtenerTodasLasReservasOcupadas
};
