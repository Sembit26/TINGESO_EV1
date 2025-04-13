package com.example.demo.Services;

import com.example.demo.Entities.Comprobante;
import com.example.demo.Repositories.ComprobanteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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

    public Comprobante crearComprobante(int precioRegular, int numPersonas,
                                        int frecuenciaCliente,
                                        String nombreCliente,
                                        List<String> cumpleaneros,
                                        List<String> nombres) {

        // Precio base sin IVA
        double precioBaseSinIva = precioRegular / 1.19;

        // Descuento por grupo
        double descuentoGrupo = 0.0;
        if (numPersonas >= 3 && numPersonas <= 5) {
            descuentoGrupo = 0.10;
        } else if (numPersonas >= 6 && numPersonas <= 10) {
            descuentoGrupo = 0.20;
        } else if (numPersonas >= 11 && numPersonas <= 15) {
            descuentoGrupo = 0.30;
        }

        // Descuento por frecuencia
        double descuentoFrecuencia = 0.0;
        if (frecuenciaCliente >= 2 && frecuenciaCliente <= 4) {
            descuentoFrecuencia = 0.10;
        } else if (frecuenciaCliente >= 5 && frecuenciaCliente <= 6) {
            descuentoFrecuencia = 0.20;
        } else if (frecuenciaCliente >= 7) {
            descuentoFrecuencia = 0.30;
        }

        // ¿Cuántos cumpleañeros pueden recibir descuento?
        int maxCumpleDescuento = 0;
        if (numPersonas >= 3 && numPersonas <= 5) maxCumpleDescuento = 1;
        else if (numPersonas >= 6 && numPersonas <= 10) maxCumpleDescuento = 2;

        int cumpleDescuentoAsignado = 0;
        List<String> pagosPorPersona = new ArrayList<>();
        double totalSinIva = 0.0;

        // Iterar sobre cada persona y calcular el descuento y el precio final con IVA incluido
        for (String nombre : nombres) {
            double descuentoAplicado = 0.0;

            // 1. Si la persona es cumpleañero y no se excedió el límite
            if (cumpleaneros.contains(nombre) && cumpleDescuentoAsignado < maxCumpleDescuento) {
                descuentoAplicado = 0.50;
                cumpleDescuentoAsignado++;
            }
            // 2. Si la persona es el cliente que reservó y no es cumpleañero (verificar cliente frecuente)
            else if (nombre.equals(nombreCliente)) {
                // Si no es cliente frecuente, aplica el descuento de cumpleaños si corresponde
                if (!cumpleaneros.contains(nombre)) {
                    descuentoAplicado = descuentoFrecuencia;
                }
                // Si no aplica ninguno de los anteriores, aplica el descuento grupal
                if (descuentoAplicado == 0.0) {
                    descuentoAplicado = descuentoGrupo;
                }
            }
            // 3. Para los demás: descuento grupal
            else {
                descuentoAplicado = descuentoGrupo;
            }

            // Calcular el pago sin IVA
            double pagoPersonaSinIva = precioBaseSinIva * (1 - descuentoAplicado);

            // Calcular el IVA sobre el pago sin IVA
            double ivaPersona = pagoPersonaSinIva * 0.19;

            // Calcular el total que paga la persona (con IVA)
            double pagoPersonaConIva = pagoPersonaSinIva + ivaPersona;

            // Acumular el total sin IVA para el total final
            totalSinIva += pagoPersonaSinIva;

            // Añadir el pago de cada persona a la lista (con IVA incluido)
            pagosPorPersona.add(nombre + ":" + String.format("%.2f", pagoPersonaConIva)); // Pago con IVA incluido
        }

        // Calcular el IVA total (para el total sin IVA acumulado)
        double ivaTotal = totalSinIva * 0.19;
        double totalConIva = totalSinIva + ivaTotal;

        // Crear el comprobante
        Comprobante comprobante = new Comprobante();
        comprobante.setPagosPorPersona(pagosPorPersona);
        comprobante.setDescuento(0.0);  // Si deseas, puedes agregar promedio de descuentos aplicados
        comprobante.setPrecio_final(Math.round(totalSinIva * 100.0) / 100.0);
        comprobante.setIva(Math.round(ivaTotal * 100.0) / 100.0);
        comprobante.setMonto_total_iva(Math.round(totalConIva * 100.0) / 100.0);

        return comprobante;
    }




}
