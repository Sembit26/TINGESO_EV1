import React, { useState } from "react";
import { useNavigate } from "react-router-dom";

const HamburgerMenu = () => {
  const [abierto, setAbierto] = useState(false);
  const [subMenuAbierto, setSubMenuAbierto] = useState(false); // Controla la visibilidad del submenú
  const navigate = useNavigate();

  const toggleMenu = () => setAbierto(!abierto);

  const irA = (ruta) => {
    navigate(ruta);
    setAbierto(false); // Cierra el menú al hacer clic
    setSubMenuAbierto(false); // Cierra el submenú al navegar
  };

  const toggleSubMenu = () => setSubMenuAbierto(!subMenuAbierto);

  return (
    <div style={{ position: "relative" }}>
      <button
        onClick={toggleMenu}
        className="hamburger-menu"
        aria-label="Menu"
        style={{ fontSize: "28px", color: "black" }}
      >
        ☰
      </button>

      {abierto && (
        <div
          style={{
            position: "absolute",
            top: "60px",
            left: "20px",
            background: "#f9f9f9",
            border: "1px solid #ccc",
            borderRadius: "8px",
            padding: "10px",
            zIndex: 1000,
            boxShadow: "0 2px 8px rgba(0,0,0,0.15)",
          }}
        >
          <div
            style={{ cursor: "pointer", padding: "5px 0" }}
            onClick={() => irA("/horariosEmpleado")}
          >
            Calendario
          </div>
          <div
            style={{ cursor: "pointer", padding: "5px 0" }}
            onClick={toggleSubMenu} // Toggle submenú cuando se hace clic en "Reportes"
          >
            Reportes
          </div>

          {/* Submenú de Reportes */}
          {subMenuAbierto && (
            <div
              style={{
                paddingLeft: "20px",
                marginTop: "5px",
                backgroundColor: "#eaeaea",
                borderRadius: "8px",
                padding: "5px",
              }}
            >
              <div
                style={{ cursor: "pointer", padding: "5px 0" }}
                onClick={() => irA("/reporteIngresoPorVuelta")}
              >
                Ingresos por vuelta
              </div>
              <div
                style={{ cursor: "pointer", padding: "5px 0" }}
                onClick={() => irA("/reporteIngresoPorPersona")} // Redirige a /ingresosPorPersona
              >
                Ingresos por persona
              </div>
            </div>
          )}
        </div>
      )}
    </div>
  );
};

export default HamburgerMenu;
