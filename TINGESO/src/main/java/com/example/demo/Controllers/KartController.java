package com.example.demo.Controllers;

import com.example.demo.Entities.Kart;
import com.example.demo.Services.KartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/karts")
@CrossOrigin("*")
public class KartController {

    @Autowired
    private KartService kartService;

    @GetMapping("/getAll")
    public List<Kart> getAllKarts() {
        return kartService.findAll();
    }

    @GetMapping("/getById/{id}")
    public Optional<Kart> getKartById(@PathVariable Long id) {
        return kartService.findById(id);
    }

    @PostMapping("/createKart")
    public Kart createKart(@RequestBody Kart kart) {
        return kartService.save(kart);
    }

    @PutMapping("/UpdateKart/{id}")
    public Kart updateKart(@PathVariable Long id, @RequestBody Kart updatedKart) {
        return kartService.update(id, updatedKart);
    }

    @DeleteMapping("/deteleKartById/{id}")
    public void deleteKart(@PathVariable Long id) {
        kartService.deleteById(id);
    }
}
