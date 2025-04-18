import axios from "axios"

const EMPLEADO_API_URL = "/api/empleados"

const loginEmployee = (data) => {
    return axios.post(`${EMPLEADO_API_URL}/login`, data);
};

const generarReservaEmpleado = (data) => {
    return axios.post(`${EMPLEADO_API_URL}/generarReservaEmpleado`, data)
}

export default {loginEmployee, generarReservaEmpleado};
