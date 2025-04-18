import React, { useState, useEffect } from 'react';
import {
  TextField, Button, Box, Typography, Container, Grid, Paper,
  MenuItem, Select, InputLabel, FormControl, Dialog, DialogTitle,
  DialogContent, DialogActions
} from '@mui/material';
import { useNavigate, useLocation } from 'react-router-dom';
import employeeService from '../services/employee.service';

const GenerateReservationEmployee = () => {
  const navigate = useNavigate();
  const location = useLocation();

  // Datos del cliente
  const [nombreCliente, setNombreCliente] = useState('');
  const [correoCliente, setCorreoCliente] = useState('');

  // Datos de la reserva
  const [numVueltasTiempoMaximo, setNumVueltasTiempoMaximo] = useState('');
  const [numPersonas, setNumPersonas] = useState('');
  const [fechaInicio, setFechaInicio] = useState('');
  const [horaInicio, setHoraInicio] = useState('');
  const [cumpleaneros, setCumpleaneros] = useState([]);
  const [nombres, setNombres] = useState([]);
  const [correos, setCorreos] = useState([]);
  const [error, setError] = useState('');

  // Estados para campos dinámicos
  const [personasFields, setPersonasFields] = useState([]);
  const [cumpleanerosFields, setCumpleanerosFields] = useState([]);

  // Modal
  const [openInfoModal, setOpenInfoModal] = useState(false);
  const [infoMessage, setInfoMessage] = useState('');

  // Recibir la fecha y hora desde el state
  const { fecha, hora } = location.state || {};

  useEffect(() => {
    if (fecha && hora) {
      setFechaInicio(fecha);
      setHoraInicio(hora);
    }
  }, [fecha, hora]);

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
  
  

  const handleGenerarReserva = async () => {
    try {
      if (!nombreCliente || !correoCliente || !numVueltasTiempoMaximo || !numPersonas || !fechaInicio || !horaInicio) {
        setError("Todos los campos son obligatorios.");
        return;
      }

      const data = {
        nombreCliente,
        correoCliente,
        numVueltasTiempoMaximo,
        numPersonas,
        fechaInicio,
        horaInicio,
        cumpleaneros,
        nombres,
        correos,
      };

      console.log("Enviando datos:", data);

      const respuesta = await employeeService.generarReservaEmpleado(data);
      console.log("Reserva generada:", respuesta.data);

      navigate('/reservasConfirmadas'); // o la ruta que consideres adecuada
    } catch (err) {
      console.error("Error al generar la reserva por empleado:", err);
      setInfoMessage("Hubo un error al generar la reserva. Intenta nuevamente.");
      setOpenInfoModal(true);
    }
  };

  const handleCloseModal = () => setOpenInfoModal(false);

  return (
    <Container maxWidth="sm">
      <Typography variant="h4" align="center" gutterBottom>
        Generar Reserva para Cliente
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
                <InputLabel id="num-vueltas-label">Número de Vueltas de Tiempo Máximo</InputLabel>
                <Select
                  labelId="num-vueltas-label"
                  value={numVueltasTiempoMaximo}
                  onChange={(e) => setNumVueltasTiempoMaximo(e.target.value)}
                  label="Número de Vueltas de Tiempo Máximo"
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
                onChange={(e) => setFechaInicio(e.target.value)}
                margin="normal"
                InputLabelProps={{ shrink: true }}
                disabled // Hace que el campo de fecha no se pueda modificar
              />
              <TextField
                label="Hora de Inicio"
                required
                fullWidth
                type="time"
                value={horaInicio}
                onChange={(e) => setHoraInicio(e.target.value)}
                margin="normal"
              />

              {numPersonas > 1 && personasFields.map((_, index) => (
                <div key={index}>
                  <TextField
                    label={`Nombre Acompañante ${index + 1}`}
                    fullWidth
                    value={nombres[index] || ''}
                    onChange={(e) => {
                      const newNombres = [...nombres];
                      newNombres[index] = e.target.value;
                      setNombres(newNombres);
                    }}
                    margin="normal"
                  />
                  <TextField
                    label={`Correo Acompañante ${index + 1}`}
                    fullWidth
                    value={correos[index] || ''}
                    onChange={(e) => {
                      const newCorreos = [...correos];
                      newCorreos[index] = e.target.value;
                      setCorreos(newCorreos);
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
                <Button variant="contained" color="primary" fullWidth onClick={handleGenerarReserva}>
                  Generar Reserva
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

export default GenerateReservationEmployee;
