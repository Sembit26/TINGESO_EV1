package com.example.demo.Controllers;

import com.example.demo.Entities.Comprobante;
import com.example.demo.Services.ComprobanteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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
}
