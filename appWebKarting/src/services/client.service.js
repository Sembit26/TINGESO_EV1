import api from "./baseUrl";  // Usando minúsculas en todo el nombre del archivo

const CLIENT_API_URL = "/api/clients"

const loginClient = (data) => {
  return api.post(`${CLIENT_API_URL}/login`, data);
};

const register = (data) => {
  return api.post(`${CLIENT_API_URL}/register`, data);
};

const generarReserva = (idCliente, data) => {
  return api.post(`${CLIENT_API_URL}/generarReserva/${idCliente}`, data)
}


export default {
  loginClient,
  register,
  generarReserva,
};
