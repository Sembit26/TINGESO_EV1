import axios from "axios"

const CLIENT_API_URL = "/api/reservas"

const horariosDisponiblesSemana = () => {
    return axios.get(`${CLIENT_API_URL}/horariosDisponiblesSemana`);
};

export default {horariosDisponiblesSemana};
