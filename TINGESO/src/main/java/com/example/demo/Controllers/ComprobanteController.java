package com.example.demo.Controllers;

import com.example.demo.Entities.Comprobante;
import com.example.demo.Services.ComprobanteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Controlador REST para gestionar comprobantes.
 */
@RestController
@RequestMapping("/api/comprobantes")
@CrossOrigin("*")
public class ComprobanteController {

    @Autowired
    private ComprobanteService comprobanteService;

    // ==================== CRUD BÁSICO ====================

    /**
     * Obtener todos los comprobantes.
     */
    @GetMapping("/getAll")
    public List<Comprobante> getAllComprobantes() {
        return comprobanteService.findAll();
    }

    /**
     * Obtener comprobante por ID.
     */
    @GetMapping("/getById/{id}")
    public Optional<Comprobante> getComprobanteById(@PathVariable Long id) {
        return comprobanteService.findById(id);
    }

    /**
     * Crear un nuevo comprobante.
     */
    @PostMapping("/createComprobante")
    public Comprobante createComprobante(@RequestBody Comprobante comprobante) {
        return comprobanteService.save(comprobante);
    }

    /**
     * Actualizar un comprobante existente por ID.
     */
    @PutMapping("/updateComprobanteById/{id}")
    public Comprobante updateComprobante(@PathVariable Long id, @RequestBody Comprobante updatedComprobante) {
        return comprobanteService.update(id, updatedComprobante);
    }

    /**
     * Eliminar comprobante por ID.
     */
    @DeleteMapping("/deleteComprobanteById/{id}")
    public void deleteComprobante(@PathVariable Long id) {
        comprobanteService.deleteById(id);
    }

    // ==================== FUNCIONES ESPECIALES ====================

    /**
     * Generar comprobante con información dinámica (desde un Map JSON).
     */
    @PostMapping("/generarComprobante")
    public Comprobante generarComprobante(@RequestBody Map<String, Object> data) {
        int precioRegular = (int) data.get("precioRegular");
        int numPersonas = (int) data.get("numPersonas");
        int frecuenciaCliente = (int) data.get("frecuenciaCliente");
        String nombreCliente = (String) data.get("nombreCliente");
        String correoCliente = (String) data.get("correoCliente");
        List<String> cumpleaneros = (List<String>) data.get("cumpleaneros");

        // Convertir Map de nombreCorreo de forma segura
        Map<String, Object> nombreCorreoRaw = (Map<String, Object>) data.get("nombreCorreo");
        Map<String, String> nombreCorreo = new HashMap<>();
        for (Map.Entry<String, Object> entry : nombreCorreoRaw.entrySet()) {
            nombreCorreo.put(entry.getKey(), (String) entry.getValue());
        }

        Comprobante comprobante = comprobanteService.crearComprobante(
                precioRegular,
                numPersonas,
                frecuenciaCliente,
                nombreCliente,
                correoCliente,
                nombreCorreo,
                cumpleaneros
        );

        return comprobanteService.save(comprobante);
    }

    /**
     * Mostrar comprobante ordenado y formateado por ID.
     */
    @GetMapping("/mostrarComprobanteOrdenado/{id}")
    public ResponseEntity<String> mostrarComprobanteOrdenado(@PathVariable Long id) {
        Optional<Comprobante> comprobanteOpt = comprobanteService.findById(id);

        if (comprobanteOpt.isPresent()) {
            String detalleFormateado = comprobanteService.formatearComprobante(comprobanteOpt.get());
            return ResponseEntity.ok(detalleFormateado);
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Comprobante no encontrado con ID: " + id);
    }
}
