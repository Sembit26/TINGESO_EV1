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

    // Obtener todos los karts
    public List<Kart> findAll() {
        return kartRepository.findAll();
    }

    // Buscar un kart por ID
    public Optional<Kart> findById(Long id) {
        return kartRepository.findById(id);
    }

    // Guardar un nuevo kart
    public Kart save(Kart kart) {
        return kartRepository.save(kart);
    }

    // Eliminar un kart por ID
    public void deleteById(Long id) {
        kartRepository.deleteById(id);
    }

    // Actualizar un kart existente
    public Kart update(Long id, Kart updatedKart) {
        return kartRepository.findById(id).map(kart -> {
            kart.setModelo(updatedKart.getModelo());
            kart.setCodificacion(updatedKart.getCodificacion());
            return kartRepository.save(kart);
        }).orElse(null);
    }
}
