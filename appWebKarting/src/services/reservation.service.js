import axios from "axios"

const RESERVA_API_URL = "/api/reservas"

const horariosDisponiblesSemana = () => {
    return axios.get(`${RESERVA_API_URL}/horariosDisponiblesSemana`);
};

const horariosOcupadosSemana = () => {
    return axios.get(`${RESERVA_API_URL}/horariosOcupadosSemana`);
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
  

export default {horariosDisponiblesSemana, horariosOcupadosSemana, obtenerReservaPorFechaYHora};
