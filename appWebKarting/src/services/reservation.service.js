import axios from "axios"

const CLIENT_API_URL = "/api/reservas"

const horariosDisponiblesSemana = () => {
    return axios.get(`${CLIENT_API_URL}/horariosDisponiblesSemana`);
};

const horariosOcupadosSemana = () => {
    return axios.get(`${CLIENT_API_URL}/horariosOcupadosSemana`);
}

export default {horariosDisponiblesSemana, horariosOcupadosSemana};
