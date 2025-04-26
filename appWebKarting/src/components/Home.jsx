import { Button, Container, Typography, Box } from '@mui/material';
import { useNavigate } from 'react-router-dom';

const Home = () => {
  const navigate = useNavigate();

  return (
    <Container maxWidth="md" sx={{ mt: 4 }}>
      <Typography variant="h3" align="center" gutterBottom>
        KartingRM: Gestión Inteligente de Operaciones de Karts
      </Typography>
      <Box
        sx={{
          display: 'flex',
          justifyContent: 'center',
          alignItems: 'center',
          gap: 2,
          mt: 3,
        }}
      >
        <Button variant="contained" color="primary" onClick={() => navigate('/loginCliente')}>
          Login para Clientes
        </Button>
        <Button variant="contained" color="primary" onClick={() => navigate('/register')}>
          Register para Clientes
        </Button>
        <Button variant="contained" color="primary" onClick={() => navigate('/loginEmpleado')}>
          Login para Empleados
        </Button>
      </Box>
    </Container>
  );
};

export default Home;
