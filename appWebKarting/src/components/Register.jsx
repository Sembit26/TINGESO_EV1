import { useState } from 'react';
import { Container, Typography, TextField, Button, Box, Paper } from '@mui/material';
import { useNavigate } from 'react-router-dom';
import clientService from '../services/client.service';

const Register = () => {
  const navigate = useNavigate();

  const [nombre, setNombre] = useState('');
  const [rut, setRut] = useState('');
  const [birthday, setBirthday] = useState('');
  const [email, setEmail] = useState('');
  const [contrasena, setContrasena] = useState('');
  const [error, setError] = useState(null);

  const handleRegister = async () => {
    try {
      const data = {
        rut,
        name: nombre,
        email,
        contrasena,
        birthday
      };

      console.log('Enviando datos de registro:', data);
      await clientService.register(data);

      navigate('/loginCliente');
    } catch (err) {
      console.error('Error en registro:', err);
      setError('Error al registrar. Verifica los datos o intenta más tarde.');
    }
  };

  return (
    <Box
      sx={{
        display: 'flex',
        justifyContent: 'center',
        alignItems: 'center',
        minHeight: '100vh',
        backgroundColor: '#f0f2f5',
        padding: 2,
      }}
    >
      <Paper elevation={3} sx={{ padding: 4, maxWidth: 500, width: '100%' }}>
        <Typography variant="h4" align="center" gutterBottom>
          Registro de Usuario
        </Typography>
        <Typography variant="body1" align="center" paragraph>
          Completa los campos para crear tu cuenta.
        </Typography>

        <TextField
          label="Nombre Completo"
          required
          fullWidth
          value={nombre}
          onChange={(e) => setNombre(e.target.value)}
          margin="normal"
        />
        <TextField
          label="RUT"
          required
          fullWidth
          value={rut}
          onChange={(e) => setRut(e.target.value)}
          margin="normal"
        />
        <TextField
          label="Fecha de Nacimiento"
          type="date"
          InputLabelProps={{ shrink: true }}
          required
          fullWidth
          value={birthday}
          onChange={(e) => setBirthday(e.target.value)}
          margin="normal"
        />
        <TextField
          label="Correo Electrónico"
          type="email"
          required
          fullWidth
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          margin="normal"
        />
        <TextField
          label="Contraseña"
          type="password"
          required
          fullWidth
          value={contrasena}
          onChange={(e) => setContrasena(e.target.value)}
          margin="normal"
        />

        {error && (
          <Typography variant="body2" color="error" align="center" mt={1}>
            {error}
          </Typography>
        )}

        <Box mt={3}>
          <Button
            variant="contained"
            fullWidth
            onClick={handleRegister}
            sx={{ mb: 1 }}
          >
            Registrarse
          </Button>
          <Button
            variant="outlined"
            fullWidth
            onClick={() => navigate('/loginCliente')}
          >
            Volver a Login
          </Button>
        </Box>
      </Paper>
    </Box>
  );
};

export default Register;
