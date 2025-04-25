import api from "./baseUrl";  // Importa la instancia de Axios configurada

const RESERVA_API_URL = "/api/reservas"

const horariosDisponiblesMes = () => {
  return api.get(`${RESERVA_API_URL}/horariosDisponiblesMes`);
};

const horariosOcupadosMes = () => {
  return api.get(`${RESERVA_API_URL}/horariosOcupadosMes`);
}

const obtenerReservaPorFechaYHora = (fechaInicio, horaInicio, horaFin) => {
    return api.get(`${RESERVA_API_URL}/obtenerReservaPorFechaYHora`, {
      params: {
        fechaInicio,
        horaInicio,
        horaFin,
      }
    });
};

// Actualizar una reserva por ID
const actualizarReservaPorId = (id, updatedReserva) => {
  return api.put(`${RESERVA_API_URL}/updateReservaById/${id}`, updatedReserva);
};

// Obtener información de una reserva por ID
const obtenerInformacionReserva = (id) => {
  return api.get(`${RESERVA_API_URL}/getInfoReserva/${id}`);
};

// Eliminar una reserva por ID
const eliminarReservaPorId = (id) => {
  return api.delete(`${RESERVA_API_URL}/deleteReservaById/${id}`);
};

// Obtener ingresos por vueltas
const obtenerIngresosPorVueltas = (fechaInicio, fechaFin) => {
  return api.get(`${RESERVA_API_URL}/ingresosPorVueltas`, {
    params: { fechaInicio, fechaFin }
  });
};

// Obtener ingresos por cantidad de personas
const obtenerIngresosPorPersonas = (fechaInicio, fechaFin) => {
  return api.get(`${RESERVA_API_URL}/ingresosPorPersonas`, {
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
