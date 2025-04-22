import axios from "axios"

const RESERVA_API_URL = "/api/reservas"

const horariosDisponiblesMes = () => {
  return axios.get(`${RESERVA_API_URL}/horariosDisponiblesMes`);
};

const horariosOcupadosMes = () => {
  return axios.get(`${RESERVA_API_URL}/horariosOcupadosMes`);
}

const obtenerReservaPorFechaYHora = (fechaInicio, horaInicio, horaFin) => {
    return axios.get(`${RESERVA_API_URL}/obtenerReservaPorFechaYHora`, {
      params: {
        fechaInicio,
        horaInicio,
        horaFin,
      }
    });
};

// Actualizar una reserva por ID
const actualizarReservaPorId = (id, updatedReserva) => {
  return axios.put(`${RESERVA_API_URL}/updateReservaById/${id}`, updatedReserva);
};

// Obtener información de una reserva por ID
const obtenerInformacionReserva = (id) => {
  return axios.get(`${RESERVA_API_URL}/getInfoReserva/${id}`);
};

// Eliminar una reserva por ID
const eliminarReservaPorId = (id) => {
  return axios.delete(`${RESERVA_API_URL}/deleteReservaById/${id}`);
};

// Obtener ingresos por vueltas
const obtenerIngresosPorVueltas = (fechaInicio, fechaFin) => {
  return axios.get(`${RESERVA_API_URL}/ingresosPorVueltas`, {
    params: { fechaInicio, fechaFin }
  });
};

// Obtener ingresos por cantidad de personas
const obtenerIngresosPorPersonas = (fechaInicio, fechaFin) => {
  return axios.get(`${RESERVA_API_URL}/ingresosPorPersonas`, {
    params: { fechaInicio, fechaFin }
  });
};

export default {
  obtenerReservaPorFechaYHora,
  horariosDisponiblesMes,
  horariosOcupadosMes,
  actualizarReservaPorId,
  obtenerInformacionReserva,
  eliminarReservaPorId,
  obtenerIngresosPorVueltas,
  obtenerIngresosPorPersonas
};
