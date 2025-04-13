package com.example.demo.Services;

import com.example.demo.Entities.Kart;
import com.example.demo.Repositories.KartRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class KartService {

    @Autowired
    private KartRepository kartRepository;

    public List<Kart> findAll() {
        return kartRepository.findAll();
    }

    public Optional<Kart> findById(Long id) {
        return kartRepository.findById(id);
    }

    public Kart save(Kart kart) {
        return kartRepository.save(kart);
    }

    public void deleteById(Long id) {
        kartRepository.deleteById(id);
    }

    public Kart update(Long id, Kart updatedKart) {
        return kartRepository.findById(id).map(kart -> {
            kart.setModelo(updatedKart.getModelo());
            kart.setCodificacion(updatedKart.getCodificacion());
            kart.setDisponible(updatedKart.isDisponible());
            return kartRepository.save(kart);
        }).orElse(null);
    }

    //Obtiene todos los karts que estan disponibles (disponibilidad = true)
    public List<Kart> finKartsByDisponibilidad(){
        return kartRepository.findByDisponibleTrue();
    }
}
