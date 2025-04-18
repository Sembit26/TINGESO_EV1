import { useState } from 'react';
import { Container, Typography, TextField, Button, Box, Paper } from '@mui/material';
import { useNavigate } from 'react-router-dom';
import clientService from '../services/client.service';

const LoginClient = () => {
  const navigate = useNavigate();

  const [email, setEmail] = useState('');
  const [contrasenia, setContrasenia] = useState('');
  const [error, setError] = useState(null);

  const handleLogin = async () => {
    try {
      const response = await clientService.loginClient({ email, contrasenia });
      sessionStorage.setItem('cliente', JSON.stringify(response.data));
      navigate('/horariosDisponibles');
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
          Iniciar Sesión
        </Typography>
        <Typography variant="body1" align="center" paragraph>
          Ingresa tus credenciales para acceder.
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
          value={contrasenia}
          onChange={(e) => setContrasenia(e.target.value)}
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
            onClick={handleLogin}
            sx={{ mb: 1 }}
          >
            Ingresar
          </Button>
          <Button
            variant="outlined"
            fullWidth
            onClick={() => navigate('/register')}
          >
            Registrarse
          </Button>
        </Box>
      </Paper>
    </Box>
  );
};

export default LoginClient;
