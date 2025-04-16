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
            return comprobanteRepository.save(comp);
        }).orElse(null);
    }

    public Comprobante crearComprobante(int precioRegular,
                                        int numPersonas,
                                        int frecuenciaCliente,
                                        String nombreCliente,
                                        List<String> cumpleaneros,
                                        List<String> nombresPersonas) {

        double precioBaseSinIva = precioRegular / 1.19;

        // Descuentos por grupo
        double descuentoGrupo = 0.0;
        if (numPersonas >= 3 && numPersonas <= 5) {
            descuentoGrupo = 0.10;
        } else if (numPersonas >= 6 && numPersonas <= 10) {
            descuentoGrupo = 0.20;
        } else if (numPersonas >= 11 && numPersonas <= 15) {
            descuentoGrupo = 0.30;
        }

        // Descuento por frecuencia del cliente
        double descuentoFrecuencia = 0.0;
        if (frecuenciaCliente >= 2 && frecuenciaCliente <= 4) {
            descuentoFrecuencia = 0.10;
        } else if (frecuenciaCliente >= 5 && frecuenciaCliente <= 6) {
            descuentoFrecuencia = 0.20;
        } else if (frecuenciaCliente >= 7) {
            descuentoFrecuencia = 0.30;
        }

        // Máximo de cumpleañeros con descuento
        int maxCumpleDescuento = 0;
        if (numPersonas >= 3 && numPersonas <= 5) maxCumpleDescuento = 1;
        else if (numPersonas >= 6 && numPersonas <= 10) maxCumpleDescuento = 2;

        int cumpleDescuentoAsignado = 0;
        List<String> pagosPorPersona = new ArrayList<>();
        double totalSinIva = 0.0;

        for (String nombre : nombresPersonas) {
            double descuentoAplicado = 0.0;
            String descuentosAplicadosTexto = "";

            // 1. Cumpleañero con descuento (máximo permitido)
            if (cumpleaneros.contains(nombre) && cumpleDescuentoAsignado < maxCumpleDescuento) {
                descuentoAplicado = 0.50;
                descuentosAplicadosTexto = "Descuento Cumpleaños 50%";
                cumpleDescuentoAsignado++;
            }

            // 2. Cliente que reservó (frecuente), si no es cumpleañero
            else if (nombre.equals(nombreCliente)) {
                if (descuentoFrecuencia > 0.0) {
                    descuentoAplicado = descuentoFrecuencia;
                    descuentosAplicadosTexto = "Descuento Frecuencia " + (int)(descuentoFrecuencia * 100) + "%";
                } else {
                    descuentoAplicado = descuentoGrupo;
                    descuentosAplicadosTexto = "Descuento Grupal " + (int)(descuentoGrupo * 100) + "%";
                }
            }

            // 3. Resto del grupo
            else {
                descuentoAplicado = descuentoGrupo;
                descuentosAplicadosTexto = "Descuento Grupal " + (int)(descuentoGrupo * 100) + "%";
            }

            double pagoSinIva = precioBaseSinIva * (1 - descuentoAplicado);
            double ivaPersona = pagoSinIva * 0.19;
            double pagoConIva = pagoSinIva + ivaPersona;

            totalSinIva += pagoSinIva;

            // Guardar información legible en el string
            String detalle = String.format(
                    "%s|Base:%.2f|%s|Monto sin IVA:%.2f|IVA:%.2f|Total:%.2f",
                    nombre,
                    precioBaseSinIva,
                    descuentosAplicadosTexto,
                    pagoSinIva,
                    ivaPersona,
                    pagoConIva
            );
            pagosPorPersona.add(detalle);
        }

        double ivaTotal = totalSinIva * 0.19;
        double totalConIva = totalSinIva + ivaTotal;

        Comprobante comprobante = new Comprobante();
        comprobante.setDescuento(0.0); // puedes modificar esto si quieres guardar el descuento promedio
        comprobante.setPrecio_final(Math.round(totalSinIva * 100.0) / 100.0);
        comprobante.setIva(Math.round(ivaTotal * 100.0) / 100.0);
        comprobante.setMonto_total_iva(Math.round(totalConIva * 100.0) / 100.0);
        comprobante.setDetallePagoPorPersona(pagosPorPersona); // Aquí guardas el detalle por persona

        return comprobante;
    }

    public void imprimirComprobante(Comprobante comprobante) {
        System.out.println("========= RESUMEN DEL COMPROBANTE =========");
        System.out.printf("Subtotal (sin IVA): %.2f\n", comprobante.getPrecio_final());
        System.out.printf("IVA: %.2f\n", comprobante.getIva());
        System.out.printf("Total con IVA: %.2f\n", comprobante.getMonto_total_iva());
        System.out.println("-------------------------------------------");
        System.out.println("Detalle por persona:");

        for (String detalle : comprobante.getDetallePagoPorPersona()) {
            String[] partes = detalle.split("\\|");
            String nombre = partes[0];
            String base = partes[1].replace("Base:", "");
            String descuento = partes[2];
            String sinIva = partes[3].replace("Monto sin IVA:", "");
            String iva = partes[4].replace("IVA:", "");
            String total = partes[5].replace("Total:", "");

            System.out.printf("- %s\n", nombre);
            System.out.printf("  Precio Base (sin IVA): %s\n", base);
            System.out.printf("  %s\n", descuento);
            System.out.printf("  Monto sin IVA: %s\n", sinIva);
            System.out.printf("  IVA: %s\n", iva);
            System.out.printf("  Total: %s\n", total);
            System.out.println();
        }
        System.out.println("===========================================");
    }

    public String formatearComprobante(Comprobante comprobante) {
        StringBuilder sb = new StringBuilder();

        sb.append("========= RESUMEN DEL COMPROBANTE =========\n");
        sb.append(String.format("Subtotal (sin IVA): %.2f\n", comprobante.getPrecio_final()));
        sb.append(String.format("IVA: %.2f\n", comprobante.getIva()));
        sb.append(String.format("Total con IVA: %.2f\n", comprobante.getMonto_total_iva()));
        sb.append("-------------------------------------------\n");
        sb.append("Detalle por persona:\n");

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
