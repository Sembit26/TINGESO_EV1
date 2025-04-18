import './App.css';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import Home from './components/Home';
import LoginClient from './components/LoginClient';
import Register from './components/Register';
import ViewReservationsCalendar from './components/ViewReservationsCalendar';
import GenerateReservation from './components/GenerateReservation';
import LoginEmployee from './components/LoginEmployee';
import ViewAllReservations from './components/ViewAllReservations';
import GenerateReservationEmployee from './components/GenerateReservationEmployee';

function App() {
  return (
    <Router>
      <div className="container app-wrapper">
        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/loginCliente" element={<LoginClient />} />
          <Route path="/loginEmpleado" element={<LoginEmployee />} />
          <Route path="/register" element={<Register />} />
          <Route path="/horariosDisponibles" element={<ViewReservationsCalendar />} />
          <Route path="/generarReserva" element={<GenerateReservation />} />
          <Route path="/horariosEmpleado" element={<ViewAllReservations />} />
          <Route path="/generarReservaEmpleado" element={<GenerateReservationEmployee />} />
        </Routes>
      </div>
    </Router>
  );
}

export default App;
