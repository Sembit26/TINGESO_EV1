package com.example.demo.Services;

import com.example.demo.Entities.Comprobante;
import com.example.demo.Repositories.ComprobanteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ComprobanteService {

    @Autowired
    private ComprobanteRepository comprobanteRepository;

    // === CRUD Básico ===

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
            return comprobanteRepository.save(comp);
        }).orElse(null);
    }

    // === Lógica de Cálculo de Comprobante ===

    public Comprobante crearComprobante(
            int precioRegular,
            int numPersonas,
            int frecuenciaCliente,
            String nombreCliente,
            String correoCliente,
            Map<String, String> nombreCorreo,
            List<String> correosCumpleaneros
    ) {
        double precioBaseSinIva = precioRegular;

        // --- Descuentos grupales ---
        double descuentoGrupo = 0.0;
        if (numPersonas >= 3 && numPersonas <= 5) descuentoGrupo = 0.10;
        else if (numPersonas >= 6 && numPersonas <= 10) descuentoGrupo = 0.20;
        else if (numPersonas >= 11 && numPersonas <= 15) descuentoGrupo = 0.30;

        // --- Descuento por frecuencia ---
        double descuentoFrecuencia = 0.0;
        if (frecuenciaCliente >= 2 && frecuenciaCliente <= 4) descuentoFrecuencia = 0.10;
        else if (frecuenciaCliente >= 5 && frecuenciaCliente <= 6) descuentoFrecuencia = 0.20;
        else if (frecuenciaCliente >= 7) descuentoFrecuencia = 0.30;

        // --- Máximo de cumpleañeros con descuento ---
        int maxCumpleDescuento = 0;
        if (numPersonas >= 3 && numPersonas <= 5) maxCumpleDescuento = 1;
        else if (numPersonas >= 6 && numPersonas <= 10) maxCumpleDescuento = 2;

        int cumpleDescuentoAsignado = 0;
        double totalSinIva = 0.0;
        List<String> pagosPorPersona = new ArrayList<>();

        // --- Procesar grupo (excepto cliente principal) ---
        for (Map.Entry<String, String> entry : nombreCorreo.entrySet()) {
            String nombre = entry.getKey();
            String correo = entry.getValue();

            if (nombre.equals(nombreCliente) && correo.equals(correoCliente)) continue;

            double descuentoAplicado = 0.0;
            String descuentosAplicadosTexto = "";

            if (correosCumpleaneros.contains(correo) && cumpleDescuentoAsignado < maxCumpleDescuento) {
                descuentoAplicado = 0.50;
                descuentosAplicadosTexto = "Descuento Cumpleaños 50%";
                cumpleDescuentoAsignado++;
            } else {
                descuentoAplicado = descuentoGrupo;
                descuentosAplicadosTexto = "Descuento Grupal " + (int) (descuentoGrupo * 100) + "%";
            }

            double pagoSinIva = precioBaseSinIva * (1 - descuentoAplicado);
            double ivaPersona = pagoSinIva * 0.19;
            double pagoConIva = pagoSinIva + ivaPersona;

            totalSinIva += pagoSinIva;

            String detalle = String.format(
                    "%s|Base:%.2f|%s|Monto sin IVA:%.2f|IVA:%.2f|Total:%.2f",
                    nombre, precioBaseSinIva, descuentosAplicadosTexto, pagoSinIva, ivaPersona, pagoConIva
            );
            pagosPorPersona.add(detalle);
        }

        // --- Procesar cliente principal ---
        double descuentoCliente = 0.0;
        String descuentosAplicadosTextoCliente = "";

        if (correosCumpleaneros.contains(correoCliente) && cumpleDescuentoAsignado < maxCumpleDescuento) {
            descuentoCliente = 0.50;
            descuentosAplicadosTextoCliente = "Descuento Cumpleaños 50%";
            cumpleDescuentoAsignado++;
        } else if (descuentoFrecuencia > 0.0) {
            descuentoCliente = descuentoFrecuencia;
            descuentosAplicadosTextoCliente = "Descuento Frecuencia " + (int)(descuentoFrecuencia * 100) + "%";
        } else {
            descuentoCliente = descuentoGrupo;
            descuentosAplicadosTextoCliente = "Descuento Grupal " + (int)(descuentoGrupo * 100) + "%";
        }

        double pagoSinIvaCliente = precioBaseSinIva * (1 - descuentoCliente);
        double ivaCliente = pagoSinIvaCliente * 0.19;
        double pagoConIvaCliente = pagoSinIvaCliente + ivaCliente;

        totalSinIva += pagoSinIvaCliente;

        String detalleCliente = String.format(
                "%s|Base:%.2f|%s|Monto sin IVA:%.2f|IVA:%.2f|Total:%.2f",
                nombreCliente, precioBaseSinIva, descuentosAplicadosTextoCliente,
                pagoSinIvaCliente, ivaCliente, pagoConIvaCliente
        );
        pagosPorPersona.add(detalleCliente);

        // --- Total general ---
        double ivaTotal = totalSinIva * 0.19;
        double totalConIva = totalSinIva + ivaTotal;

        Comprobante comprobante = new Comprobante();
        comprobante.setDescuento(0.0); // Opcional: se puede calcular descuento total o promedio
        comprobante.setPrecio_final(Math.round(totalSinIva * 100.0) / 100.0);
        comprobante.setIva(Math.round(ivaTotal * 100.0) / 100.0);
        comprobante.setMonto_total_iva(Math.round(totalConIva * 100.0) / 100.0);
        comprobante.setDetallePagoPorPersona(pagosPorPersona);

        return comprobante;
    }

    // === Formatear Comprobante para imprimir ===

    public String formatearComprobante(Comprobante comprobante) {
        StringBuilder sb = new StringBuilder();

        sb.append("========= RESUMEN DEL COMPROBANTE =========\n");
        sb.append(String.format("Subtotal (sin IVA): %.2f\n", comprobante.getPrecio_final()));
        sb.append(String.format("IVA: %.2f\n", comprobante.getIva()));
        sb.append(String.format("Total con IVA: %.2f\n", comprobante.getMonto_total_iva()));
        sb.append("-------------------------------------------\n");
        sb.append("Detalle por persona:\n\n");

        for (String detalle : comprobante.getDetallePagoPorPersona()) {
            String[] partes = detalle.split("\\|");
            String nombre = partes[0];
            String base = partes[1].replace("Base:", "");
            String descuento = partes[2];
            String sinIva = partes[3].replace("Monto sin IVA:", "");
            String iva = partes[4].replace("IVA:", "");
            String total = partes[5].replace("Total:", "");

            sb.append("- ").append(nombre).append("\n");
            sb.append("  Precio Base (sin IVA): ").append(base).append("\n");
            sb.append("  ").append(descuento).append("\n");
            sb.append("  Monto sin IVA: ").append(sinIva).append("\n");
            sb.append("  IVA: ").append(iva).append("\n");
            sb.append("  Total: ").append(total).append("\n\n");
        }

        sb.append("===========================================\n");
        return sb.toString();
    }
}
