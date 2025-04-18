import './App.css';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import Home from './components/Home';
import Login from './components/Login';
import Register from './components/Register';
import ViewReservationsCalendar from './components/ViewReservationsCalendar';
import GenerateReservation from './components/GenerateReservation';

function App() {
  return (
    <Router>
      <div className="container app-wrapper">
        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />
          <Route path="/horariosDisponibles" element={<ViewReservationsCalendar />} />
          <Route path="/generarReserva" element={<GenerateReservation />} />
        </Routes>
      </div>
    </Router>
  );
}

export default App;
