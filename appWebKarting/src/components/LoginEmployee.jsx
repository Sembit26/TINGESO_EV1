import { useState } from 'react';
import {
  Container,
  Typography,
  TextField,
  Button,
  Box,
  Paper
} from '@mui/material';
import { useNavigate } from 'react-router-dom';
import empleadoService from '../services/employee.service';

const LoginEmployee = () => {
  const navigate = useNavigate();

  const [email, setEmail] = useState('');
  const [contrasena, setContrasena] = useState('');
  const [error, setError] = useState(null);

  const handleLogin = async () => {
    try {
      const response = await empleadoService.loginEmployee({ email, contrasena });
      sessionStorage.setItem('empleado', JSON.stringify(response.data));
      navigate('/horariosEmpleado'); // Cambia esto al path de la vista principal del empleado
    } catch (err) {
      setError('Usuario no encontrado: Correo o contraseña incorrectos');
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
      <Paper elevation={3} sx={{ padding: 4, maxWidth: 400, width: '100%' }}>
        <Typography variant="h4" align="center" gutterBottom>
          Iniciar Sesión - Empleado
        </Typography>
        <Typography variant="body1" align="center" paragraph>
          Ingresa tus credenciales para acceder al sistema.
        </Typography>

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
          <Button variant="contained" fullWidth onClick={handleLogin}>
            Ingresar
          </Button>
        </Box>
      </Paper>
    </Box>
  );
};

export default LoginEmployee;
