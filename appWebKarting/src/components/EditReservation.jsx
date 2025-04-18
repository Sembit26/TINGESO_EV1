import React from 'react';
import { useLocation } from 'react-router-dom';

const EditReservation = () => {
  const { state } = useLocation();
  const { fecha, horaInicio, horaFin } = state;

  return (
    <div>
      <h2>Editar Reserva</h2>
      <p><strong>Fecha:</strong> {fecha}</p>
      <p><strong>Hora inicio:</strong> {horaInicio}</p>
      <p><strong>Hora fin:</strong> {horaFin}</p>

      {/* Aquí puedes agregar los campos para editar y un botón para guardar los cambios */}
    </div>
  );
};

export default EditReservation;
