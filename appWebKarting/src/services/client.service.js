import axios from "axios"

const CLIENT_API_URL = "/api/clients"

const loginClient = (data) => {
    return axios.post(`${CLIENT_API_URL}/login`, data);
};

const register = (data) => {
    return axios.post(`${CLIENT_API_URL}/register`, data);
};

const generarReserva = (idCliente, data) => {
    return axios.post(`${CLIENT_API_URL}/generarReserva/${idCliente}`, data)
}

export default {loginClient, register, generarReserva};
