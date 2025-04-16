package com.example.demo.Controllers;

import com.example.demo.Entities.Comprobante;
import com.example.demo.Services.ComprobanteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/comprobantes")
public class ComprobanteController {

    @Autowired
    private ComprobanteService comprobanteService;

    @GetMapping("/getAll")
    public List<Comprobante> getAllComprobantes() {
        return comprobanteService.findAll();
    }

    @GetMapping("/getById/{id}")
    public Optional<Comprobante> getComprobanteById(@PathVariable Long id) {
        return comprobanteService.findById(id);
    }

    @PostMapping("/createComprobante")
    public Comprobante createComprobante(@RequestBody Comprobante comprobante) {
        return comprobanteService.save(comprobante);
    }

    @PutMapping("/updateComprobanteById/{id}")
    public Comprobante updateComprobante(@PathVariable Long id, @RequestBody Comprobante updatedComprobante) {
        return comprobanteService.update(id, updatedComprobante);
    }

    @DeleteMapping("/deleteComprobanteById/{id}")
    public void deleteComprobante(@PathVariable Long id) {
        comprobanteService.deleteById(id);
    }

    @PostMapping("/generarComprobante")
    public Comprobante generarComprobante(@RequestBody Map<String, Object> data) {
        int precioRegular = (int) data.get("precioRegular");
        int numPersonas = (int) data.get("numPersonas");
        int frecuenciaCliente = (int) data.get("frecuenciaCliente");
        String nombreCliente = (String) data.get("nombreCliente");

        List<String> cumpleaneros = (List<String>) data.get("cumpleaneros");
        List<String> nombresPersonas = (List<String>) data.get("nombresPersonas");

        Comprobante comprobante = comprobanteService.crearComprobante(
                precioRegular,
                numPersonas,
                frecuenciaCliente,
                nombreCliente,
                cumpleaneros,
                nombresPersonas
        );

        return comprobanteService.save(comprobante);
    }

    @GetMapping("/mostrarComprobanteOrdenado/{id}")
    public ResponseEntity<String> mostrarComprobanteOrdenado(@PathVariable Long id) {
        Optional<Comprobante> comprobanteOpt = comprobanteService.findById(id);
        if (comprobanteOpt.isPresent()) {
            String detalleFormateado = comprobanteService.formatearComprobante(comprobanteOpt.get());
            return ResponseEntity.ok(detalleFormateado);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Comprobante no encontrado con ID: " + id);
        }
    }


}
