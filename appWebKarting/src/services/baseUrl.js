import axios from "axios";

// Obtiene la URL base de la API desde las variables de entorno de Vite
const baseURL = import.meta.env.VITE_API_URL;

// Configura Axios con la baseURL
const api = axios.create({
  baseURL: baseURL || "http://localhost:3000", // Usa una URL por defecto si no se define
  headers: {
    "Content-Type": "application/json",
  },
});

export default api;
