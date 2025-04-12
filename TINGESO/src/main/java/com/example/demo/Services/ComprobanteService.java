package com.example.demo.Services;

import com.example.demo.Entities.Comprobante;
import com.example.demo.Repositories.ComprobanteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ComprobanteService {

    @Autowired
    private ComprobanteRepository comprobanteRepository;

    public List<Comprobante> findAll() {
        return comprobanteRepository.findAll();
    }

    public Optional<Comprobante> findById(Long id) {
        return comprobanteRepository.findById(id);
    }

    public Comprobante save(Comprobante comprobante) {
        return comprobanteRepository.save(comprobante);
    }

    public void deleteById(Long id) {
        comprobanteRepository.deleteById(id);
    }

    public Comprobante update(Long id, Comprobante updatedComprobante) {
        return comprobanteRepository.findById(id).map(comp -> {
            comp.setDescuento(updatedComprobante.getDescuento());
            comp.setPrecio_final(updatedComprobante.getPrecio_final());
            comp.setIva(updatedComprobante.getIva());
            comp.setMonto_total_iva(updatedComprobante.getMonto_total_iva());
            comp.setPagosPorPersona(updatedComprobante.getPagosPorPersona());
            return comprobanteRepository.save(comp);
        }).orElse(null);
    }
}
