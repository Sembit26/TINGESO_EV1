import api from "./baseUrl";  // Importa tu instancia de Axios configurada

const EMPLEADO_API_URL = "/api/empleados"


const loginEmployee = (data) => {
    return api.post(`${EMPLEADO_API_URL}/login`, data);
};

const generarReservaEmpleado = (data) => {
    return api.post(`${EMPLEADO_API_URL}/generarReservaEmpleado`, data)
}

export default { loginEmployee, generarReservaEmpleado };
