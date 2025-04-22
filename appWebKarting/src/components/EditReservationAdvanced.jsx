import React, { useState, useEffect } from 'react';
import {
  TextField, Button, Box, Typography, Container, Grid, Paper,
  MenuItem, Select, InputLabel, FormControl, Dialog, DialogTitle,
  DialogContent, DialogActions
} from '@mui/material';
import { useNavigate, useLocation } from 'react-router-dom';
import employeeService from '../services/employee.service';
import reservationService from '../services/reservation.service';

const EditReservationAdvanced = () => {
  const navigate = useNavigate();
  const { state } = useLocation();
  const { id, fecha, horaInicio } = state || {};

  // Datos de reserva
  const [nombreCliente, setNombreCliente] = useState('');
  const [correoCliente, setCorreoCliente] = useState('');
  const [numVueltasTiempoMaximo, setNumVueltasTiempoMaximo] = useState('');
  const [numPersonas, setNumPersonas] = useState('');
  const [fechaInicio, setFechaInicio] = useState('');
  const [horaInicioState, setHoraInicioState] = useState('');

  const [cumpleaneros, setCumpleaneros] = useState([]);
  const [nombres, setNombres] = useState([]);
  const [correos, setCorreos] = useState([]);

  const [personasFields, setPersonasFields] = useState([]);
  const [cumpleanerosFields, setCumpleanerosFields] = useState([]);

  const [error, setError] = useState('');
  const [openInfoModal, setOpenInfoModal] = useState(false);
  const [infoMessage, setInfoMessage] = useState('');

  useEffect(() => {
    if (fecha && horaInicio) {
      setFechaInicio(fecha);
      setHoraInicioState(horaInicio);
    }
  }, [fecha, horaInicio]);

  useEffect(() => {
    if (numPersonas > 0) {
      const camposPersonas = numPersonas > 1 ? numPersonas - 1 : 0;

      let camposCumple = 0;
      if (numPersonas >= 3 && numPersonas <= 5) {
        camposCumple = 1;
      } else if (numPersonas >= 6 && numPersonas <= 10) {
        camposCumple = 2;
      }

      setPersonasFields(new Array(camposPersonas).fill(''));
      setCumpleanerosFields(new Array(camposCumple).fill(''));
    }
  }, [numPersonas]);

  const handleGuardarCambios = async () => {
    try {
      if (!nombreCliente || !correoCliente || !numVueltasTiempoMaximo || !numPersonas || !fechaInicio || !horaInicioState) {
        setError("Todos los campos son obligatorios.");
        return;
      }

      // 1. Eliminar reserva anterior
      await reservationService.eliminarReservaPorId(id);
      console.log(`Reserva con ID ${id} eliminada.`);

      // 2. Generar nueva reserva
      const data = {
        nombreCliente,
        correoCliente,
        numVueltasTiempoMaximo,
        numPersonas,
        fechaInicio,
        horaInicio: horaInicioState,
        cumpleaneros,
        nombres,
        correos,
      };

      const respuesta = await employeeService.generarReservaEmpleado(data);
      console.log("Nueva reserva generada:", respuesta.data);

      // 3. Navegar
      navigate('/horariosEmpleado');
    } catch (err) {
      console.error("Error al guardar los cambios:", err);
      setInfoMessage("Hubo un error al guardar los cambios. Intenta nuevamente.");
      setOpenInfoModal(true);
    }
  };

  const handleCloseModal = () => setOpenInfoModal(false);

  return (
    <Container maxWidth="sm">
      <Typography variant="h4" align="center" gutterBottom>
        Editar Reserva
      </Typography>

      {error && (
        <Typography variant="body2" color="error" align="center">
          {error}
        </Typography>
      )}

      <Grid container spacing={2} justifyContent="center">
        <Grid item xs={12}>
          <Paper elevation={3} style={{ padding: '2rem' }}>
            <Box component="form" onSubmit={(e) => e.preventDefault()}>
              <TextField
                label="Nombre del Cliente"
                required
                fullWidth
                value={nombreCliente}
                onChange={(e) => setNombreCliente(e.target.value)}
                margin="normal"
              />
              <TextField
                label="Correo del Cliente"
                required
                fullWidth
                value={correoCliente}
                onChange={(e) => setCorreoCliente(e.target.value)}
                margin="normal"
              />

              <FormControl fullWidth margin="normal">
                <InputLabel id="num-vueltas-label">Número de Vueltas o Tiempo Máximo</InputLabel>
                <Select
                  labelId="num-vueltas-label"
                  value={numVueltasTiempoMaximo}
                  onChange={(e) => setNumVueltasTiempoMaximo(e.target.value)}
                  label="Número de Vueltas o Tiempo Máximo"
                >
                  {[10, 15, 20].map((value) => (
                    <MenuItem key={value} value={value}>{value}</MenuItem>
                  ))}
                </Select>
              </FormControl>

              <FormControl fullWidth margin="normal">
                <InputLabel id="num-personas-label">Número de Personas</InputLabel>
                <Select
                  labelId="num-personas-label"
                  value={numPersonas}
                  onChange={(e) => setNumPersonas(e.target.value)}
                  label="Número de Personas"
                >
                  {[...Array(15)].map((_, index) => (
                    <MenuItem key={index + 1} value={index + 1}>{index + 1}</MenuItem>
                  ))}
                </Select>
              </FormControl>

              <TextField
                label="Fecha de Reserva"
                required
                fullWidth
                type="date"
                value={fechaInicio}
                margin="normal"
                InputLabelProps={{ shrink: true }}
                disabled
              />
              <TextField
                label="Hora de Inicio"
                required
                fullWidth
                type="time"
                value={horaInicioState}
                onChange={(e) => setHoraInicioState(e.target.value)}
                margin="normal"
              />

              {numPersonas > 1 && personasFields.map((_, index) => (
                <div key={index}>
                  <TextField
                    label={`Nombre Acompañante ${index + 1}`}
                    fullWidth
                    value={nombres[index] || ''}
                    onChange={(e) => {
                      const nuevos = [...nombres];
                      nuevos[index] = e.target.value;
                      setNombres(nuevos);
                    }}
                    margin="normal"
                  />
                  <TextField
                    label={`Correo Acompañante ${index + 1}`}
                    fullWidth
                    value={correos[index] || ''}
                    onChange={(e) => {
                      const nuevos = [...correos];
                      nuevos[index] = e.target.value;
                      setCorreos(nuevos);
                    }}
                    margin="normal"
                  />
                </div>
              ))}

              {numPersonas >= 3 && cumpleanerosFields.map((_, index) => (
                <div key={index}>
                  <TextField
                    label={`Correo Cumpleañero ${index + 1}`}
                    fullWidth
                    value={cumpleaneros[index] || ''}
                    onChange={(e) => {
                      const nuevos = [...cumpleaneros];
                      nuevos[index] = e.target.value;
                      setCumpleaneros(nuevos);
                    }}
                    margin="normal"
                  />
                </div>
              ))}

              <Box mt={2}>
                <Button variant="contained" color="primary" fullWidth onClick={handleGuardarCambios}>
                  Guardar cambios
                </Button>
              </Box>
            </Box>
          </Paper>
        </Grid>
      </Grid>

      <Dialog open={openInfoModal} onClose={handleCloseModal}>
        <DialogTitle>Error</DialogTitle>
        <DialogContent>
          <Typography>{infoMessage}</Typography>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseModal} color="primary">Cerrar</Button>
        </DialogActions>
      </Dialog>
    </Container>
  );
};

export default EditReservationAdvanced;
