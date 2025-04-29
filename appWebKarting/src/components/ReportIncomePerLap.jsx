import React, { useState } from "react";
import reservaService from "../services/reservation.service";
import HamburgerMenu from "../components/HamburgerMenu"; // Importar el HamburgerMenu

// Función para mapear el número del mes al nombre del mes
const monthNames = [
  "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
  "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
];

const formatMonth = (month) => {
  const [year, monthNumber] = month.split("-");
  const monthName = monthNames[parseInt(monthNumber, 10) - 1]; // Mapea el mes al nombre correspondiente
  return `${monthName} del ${year}`;
};

const ReportIncomePerLap = () => {
  const [fechaInicio, setFechaInicio] = useState("");
  const [fechaFin, setFechaFin] = useState("");
  const [ingresos, setIngresos] = useState(null);
  const [error, setError] = useState(null);

  // Función para manejar la búsqueda
  const handleSearch = async (e) => {
    e.preventDefault();
    if (!fechaInicio || !fechaFin) {
      setError("Ambas fechas son necesarias.");
      return;
    }
    try {
      const response = await reservaService.obtenerIngresosPorVueltas(fechaInicio, fechaFin);
      setIngresos(response.data); // Guardamos los datos en el estado
      setError(null); // Limpiamos cualquier error previo
    } catch (err) {
      setError("Error al obtener los datos.");
      console.error(err);
    }
  };

  // Función para calcular los totales por fila
  const calculateRowTotal = (rowData) => {
    return rowData.reduce((acc, val) => acc + (val || 0), 0);
  };

  // Función para renderizar los ingresos en una tabla con el formato solicitado
  const renderTable = () => {
    if (!ingresos) return null;

    // Obtenemos las claves de los meses (en el formato '2024-09', '2024-10', etc.)
    const meses = Object.keys(ingresos);

    // Estructura para mostrar los datos (filas de 10, 15, 20 vueltas, y total)
    const data = [
      { label: "10 vueltas", values: meses.map(mes => ingresos[mes]["10"]) },
      { label: "15 vueltas", values: meses.map(mes => ingresos[mes]["15"]) },
      { label: "20 vueltas", values: meses.map(mes => ingresos[mes]["20"]) },
      { label: "Total", values: meses.map(mes => ingresos[mes].TOTAL) },
    ];

    // Calculamos los totales de cada fila
    const totals = data.map(row => calculateRowTotal(row.values));

    return (
      <table style={{ width: "100%", borderCollapse: "collapse" }}>
        <thead>
          <tr style={{ backgroundColor: "#f4f4f4", textAlign: "center" }}>
            <th style={headerStyle}>Vueltas</th>
            {meses.map((mes) => (
              <th key={mes} style={headerStyle}>
                {formatMonth(mes)} {/* Aquí ponemos los meses con nombre completo */}
              </th>
            ))}
            <th style={headerStyle}>Total</th> {/* Columna total */}
          </tr>
        </thead>
        <tbody>
          {data.map((row, index) => (
            <tr key={index} style={index % 2 === 0 ? rowEvenStyle : rowOddStyle}>
              <td style={cellStyle}>{row.label}</td>
              {row.values.map((value, idx) => (
                <td key={idx} style={cellStyle}>
                  {value !== null ? value : "No disponible"} {/* Mostrar "No disponible" si el valor es nulo */}
                </td>
              ))}
              <td style={cellStyle}>{totals[index]}</td> {/* Total por fila */}
            </tr>
          ))}
        </tbody>
      </table>
    );
  };

  // Estilos para la tabla
  const headerStyle = {
    border: "1px solid #ddd",
    padding: "8px",
    backgroundColor: "#f2f2f2",
    fontWeight: "bold",
    textAlign: "center",
  };

  const cellStyle = {
    border: "1px solid #ddd",
    padding: "8px",
    textAlign: "center",
  };

  const rowEvenStyle = {
    backgroundColor: "#f9f9f9",
  };

  const rowOddStyle = {
    backgroundColor: "#ffffff",
  };

  return (
    <div style={{ padding: "20px", fontFamily: "Arial, sans-serif" }}>
      {/* Agregar el HamburgerMenu al componente */}
      <HamburgerMenu />

      <h2 style={{ textAlign: "center" }}>Reporte de Ingresos por Vuelta</h2>
      
      {/* Formulario para seleccionar fechas */}
      <form onSubmit={handleSearch} style={{ marginBottom: "20px", textAlign: "center" }}>
        <div style={{ marginBottom: "10px" }}>
          <label>Fecha de inicio:</label>
          <input
            type="date"
            value={fechaInicio}
            onChange={(e) => setFechaInicio(e.target.value)}
            style={{ padding: "8px", marginLeft: "10px" }}
          />
        </div>
        <div style={{ marginBottom: "10px" }}>
          <label>Fecha de fin:</label>
          <input
            type="date"
            value={fechaFin}
            onChange={(e) => setFechaFin(e.target.value)}
            style={{ padding: "8px", marginLeft: "10px" }}
          />
        </div>
        <button type="submit" style={{ padding: "10px 20px", backgroundColor: "#4CAF50", color: "white", border: "none", borderRadius: "4px", cursor: "pointer" }}>
          Generar reporte
        </button>
      </form>

      {/* Mostrar el error si hay */}
      {error && <p style={{ color: "red", textAlign: "center" }}>{error}</p>}

      {/* Mostrar la tabla si hay datos */}
      {renderTable()}
    </div>
  );
};

export default ReportIncomePerLap;
