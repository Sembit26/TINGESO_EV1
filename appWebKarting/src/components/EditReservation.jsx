import React, { useEffect, useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import reservaService from '../services/reservation.service';

const EditReservation = () => {
  const { state } = useLocation();
  const { id, fecha, horaInicio, horaFin } = state;
  const navigate = useNavigate();  // Usamos el hook useNavigate

  const [reservaInfo, setReservaInfo] = useState(null);
  const [loading, setLoading] = useState(true);
  const [nombreCliente, setNombreCliente] = useState('');

  useEffect(() => {
    reservaService.obtenerInformacionReserva(id)
      .then(response => {
        setReservaInfo(response.data);
        setLoading(false);
      })
      .catch(error => {
        console.error("Error al obtener información de la reserva:", error);
        setLoading(false);
      });
  }, [id]);

  const handleActualizarReserva = () => {
    const updatedReserva = {
      nombreCliente: nombreCliente
    };

    reservaService.actualizarReservaPorId(id, updatedReserva)
      .then(() => {
        alert('Reserva actualizada correctamente');
      })
      .catch(error => {
        console.error("Error al actualizar la reserva:", error);
        alert('Error al actualizar la reserva');
      });
  };

  const handleEliminarReserva = () => {
    if (window.confirm('¿Seguro que deseas eliminar esta reserva?')) {
      reservaService.eliminarReservaPorId(id)
        .then(() => {
          alert('Reserva eliminada correctamente');
          navigate('/horariosEmpleado');  // Redirige a la página de edición de reservas
        })
        .catch(error => {
          console.error("Error al eliminar la reserva:", error);
          alert('Error al eliminar la reserva');
        });
    }
  };

  const handleEditarReservaAvanzado = () => {
    navigate('/editarReservaAvanzada', {
      state: {
        id,
        fecha,
        horaInicio,
        horaFin
      }
    });
  };

  if (loading) {
    return <div style={styles.centered}>Cargando...</div>;
  }

  if (!reservaInfo) {
    return <div style={styles.centered}>No se encontró la reserva.</div>;
  }

  return (
    <div style={styles.container}>
      <h2>Editar Reserva</h2>
      <div style={styles.contentWrapper}>
        <div style={styles.infoContainer}>
          <pre>{reservaInfo}</pre>
        </div>

        <div style={styles.formContainer}>
          <label htmlFor="nombreCliente">Ingrese nombre cliente:</label>
          <input
            type="text"
            id="nombreCliente"
            value={nombreCliente}
            onChange={(e) => setNombreCliente(e.target.value)}
            style={styles.inputWhite}  // Cambié el fondo a blanco
          />

          <div style={styles.buttonsContainer}>
            <button onClick={handleActualizarReserva} style={styles.buttonUpdate}>
              Actualizar Nombre Cliente
            </button>
            <button onClick={handleEliminarReserva} style={styles.buttonDelete}>
              Eliminar Reserva
            </button>
          </div>

          {/* Botón para Editar Reserva Avanzado */}
          <button onClick={handleEditarReservaAvanzado} style={styles.buttonAdvanced}>
            Edición avanzada
          </button>
        </div>
      </div>
    </div>
  );
};

const styles = {
  container: {
    padding: '20px',
    textAlign: 'center',
  },
  contentWrapper: {
    display: 'flex',
    justifyContent: 'space-around',
    marginTop: '20px',
  },
  infoContainer: {
    width: '45%',
    padding: '20px',
    border: '1px solid #ccc',
    borderRadius: '8px',
    backgroundColor: '#f9f9f9',
  },
  formContainer: {
    width: '45%',
    padding: '20px',
    border: '1px solid #ccc',
    borderRadius: '8px',
    backgroundColor: '#eef5ff',
    display: 'flex',
    flexDirection: 'column',
    gap: '10px',
  },
  inputWhite: {
    padding: '8px',
    fontSize: '16px',
    backgroundColor: 'white',  // Fondo blanco
    color: 'black',  // Texto oscuro para que se vea bien
  },
  buttonsContainer: {
    display: 'flex',
    justifyContent: 'space-between',
  },
  buttonUpdate: {
    padding: '6px 12px',
    backgroundColor: '#007BFF',
    color: 'white',
    fontSize: '14px',
    border: 'none',
    borderRadius: '4px',
    cursor: 'pointer',
    width: '45%',  // Botón más pequeño
  },
  buttonDelete: {
    padding: '6px 12px',
    backgroundColor: '#FF4C4C',  // Rojo
    color: 'white',
    fontSize: '14px',
    border: 'none',
    borderRadius: '4px',
    cursor: 'pointer',
    width: '45%',  // Botón más pequeño
  },
  buttonAdvanced: {
    padding: '6px 12px',
    backgroundColor: '#FFA500',  // Naranja
    color: 'white',
    fontSize: '14px',
    border: 'none',
    borderRadius: '4px',
    cursor: 'pointer',
    marginTop: '10px',
  },
  centered: {
    display: 'flex',
    justifyContent: 'center',
    alignItems: 'center',
    height: '100vh',
    fontSize: '20px',
  },
};

export default EditReservation;
