import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useLocation } from 'react-router-dom';
import { TextField, Button, Box, Typography, Container, Grid, Paper, MenuItem, Select, InputLabel, FormControl } from '@mui/material';
import clientService from '../services/client.service';
import { Dialog, DialogTitle, DialogContent, DialogActions } from '@mui/material';

const GenerateReservation = () => {
  const navigate = useNavigate();
  const location = useLocation();

  // Estados para el formulario
  const [numVueltasTiempoMaximo, setNumVueltasTiempoMaximo] = useState('');
  const [numPersonas, setNumPersonas] = useState('');
  const [fechaInicio, setFechaInicio] = useState('');
  const [horaInicio, setHoraInicio] = useState('');
  const [cumpleaneros, setCumpleaneros] = useState([]);
  const [nombres, setNombres] = useState([]);
  const [correos, setCorreos] = useState([]);
  const [error, setError] = useState('');

  // Estados para el modal de error
  const [openInfoModal, setOpenInfoModal] = useState(false);
  const [infoMessage, setInfoMessage] = useState('');

  // Estados dinámicos para los campos de nombres y correos
  const [personasFields, setPersonasFields] = useState([]);
  const [cumpleanerosFields, setCumpleanerosFields] = useState([]);

  // Este useEffect se asegura de llenar los campos de fecha y hora si vienen desde el calendario
  useEffect(() => {
    if (location.state?.fecha && location.state?.hora) {
      setFechaInicio(location.state.fecha);  // Prellenar la fecha
      setHoraInicio(location.state.hora);    // Prellenar la hora
    }
  }, [location.state]);

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
      console.log("Iniciando generación de reserva...");

      // Validación de campos
      if (!numVueltasTiempoMaximo || !numPersonas || !fechaInicio || !horaInicio) {
        setError("Todos los campos son obligatorios.");
        console.warn("Faltan campos obligatorios");
        return;
      }

      // Verificamos datos ingresados
      console.log("Datos del formulario:");
      console.log({
        numVueltasTiempoMaximo,
        numPersonas,
        fechaInicio,
        horaInicio,
        cumpleaneros,
        nombres,
        correos,
      });

      // Obtenemos cliente desde sessionStorage
      const clienteRaw = sessionStorage.getItem('cliente');
      if (!clienteRaw) {
        console.error("No se encontró el cliente en sessionStorage");
        setError("Error interno: cliente no encontrado");
        return;
      }

      const cliente = JSON.parse(clienteRaw);
      console.log("Cliente obtenido:", cliente);

      const idCliente = cliente.id;
      console.log("ID Cliente:", idCliente);

      const data = {
        numVueltasTiempoMaximo,
        numPersonas,
        fechaInicio,
        horaInicio,
        cumpleaneros,
        nombres,
        correos,
      };

      console.log("Datos que se enviarán al backend:", data);

      // Llamamos al servicio para generar la reserva
      const respuesta = await clientService.generarReserva(idCliente, data);
      console.log("Respuesta del servicio:", respuesta);

      // Navegamos a la confirmación si todo sale bien
      navigate('/horariosDisponibles');
    } catch (err) {
      console.error("Error al generar la reserva:", err);
      setInfoMessage('Hola, se produjo un error, intente en otro horario.');
      setOpenInfoModal(true);
    }
  };

  const handleCloseModal = () => {
    setOpenInfoModal(false);
  };

  return (
    <Container maxWidth="sm">
      <Typography variant="h4" align="center" gutterBottom>
        Generar Reserva
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
              {/* Dropdown para el número de vueltas de tiempo máximo (10, 15, 20) */}
              <FormControl fullWidth margin="normal">
                <InputLabel id="num-vueltas-label">Número de Vueltas o Tiempo Máximo (minutos)</InputLabel>
                <Select
                  labelId="num-vueltas-label"
                  value={numVueltasTiempoMaximo}
                  onChange={(e) => setNumVueltasTiempoMaximo(e.target.value)}
                  label="Número de Vueltas o Tiempo Máximo"
                >
                  {[10, 15, 20].map((value) => (
                    <MenuItem key={value} value={value}>
                      {value}
                    </MenuItem>
                  ))}
                </Select>
              </FormControl>

              {/* Dropdown para seleccionar el número de personas (1 a 15) */}
              <FormControl fullWidth margin="normal">
                <InputLabel id="num-personas-label">Número de Personas</InputLabel>
                <Select
                  labelId="num-personas-label"
                  value={numPersonas}
                  onChange={(e) => setNumPersonas(e.target.value)}
                  label="Número de Personas"
                >
                  {[...Array(15)].map((_, index) => (
                    <MenuItem key={index + 1} value={index + 1}>
                      {index + 1}
                    </MenuItem>
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
                InputLabelProps={{
                  shrink: true,
                }}
                disabled={!!location.state?.fecha}  // Deshabilitar si fecha está presente en el state
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

              {/* Si el número de personas es mayor que 1, generamos campos para nombres y correos */}
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

              {/* Mostrar los campos para los correos de los cumpleañeros solo si hay más de 2 personas */}
              {numPersonas >= 3 && cumpleanerosFields.map((_, index) => (
                <div key={index}>
                  <TextField
                    label={`Correo Cumpleañero ${index + 1} (Debe estar registrado para aplicar el descuento)`}
                    fullWidth
                    value={cumpleaneros[index] || ''}
                    onChange={(e) => {
                      const newCumpleaneros = [...cumpleaneros];
                      newCumpleaneros[index] = e.target.value;
                      setCumpleaneros(newCumpleaneros);
                    }}
                    margin="normal"
                  />
                </div>
              ))}

              <Box mt={2}>
                <Button
                  variant="contained"
                  color="primary"
                  fullWidth
                  onClick={handleGenerarReserva}
                >
                  Generar Reserva
                </Button>
              </Box>
            </Box>
          </Paper>
        </Grid>
      </Grid>

      {/* Modal de error */}
      <Dialog open={openInfoModal} onClose={handleCloseModal}>
        <DialogTitle>Error</DialogTitle>
        <DialogContent>
          <Typography>{infoMessage}</Typography>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseModal} color="primary">
            Cerrar
          </Button>
        </DialogActions>
      </Dialog>
    </Container>
  );
};

export default GenerateReservation;
