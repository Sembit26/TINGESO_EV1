import httClient from "../http-common";

const CLIENT_API_URL = "/api/clients"

const loginClient = (data) => {
  return httClient.post(`${CLIENT_API_URL}/login`, data);
};

const register = (data) => {
  return httClient.post(`${CLIENT_API_URL}/register`, data);
};

const generarReserva = (idCliente, data) => {
  return httClient.post(`${CLIENT_API_URL}/generarReserva/${idCliente}`, data)
}


export default {
  loginClient,
  register,
  generarReserva,
};
