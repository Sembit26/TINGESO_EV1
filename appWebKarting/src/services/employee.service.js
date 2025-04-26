import httClient from "../http-common";

const EMPLEADO_API_URL = "/api/empleados"


const loginEmployee = (data) => {
    return httClient.post(`${EMPLEADO_API_URL}/login`, data);
};

const generarReservaEmpleado = (data) => {
    return httClient.post(`${EMPLEADO_API_URL}/generarReservaEmpleado`, data)
}

export default { loginEmployee, generarReservaEmpleado };
