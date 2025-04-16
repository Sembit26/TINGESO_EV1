package com.example.demo.Entities;

import lombok.*;

import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "comprobantes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Comprobante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(unique = true, nullable = false)
    private Long id;

    // Ej: "Sebastián Del Solar|Tarifa Base:20000|Desc. Grupo:10%|Desc. Especial:5%|Monto:17100|IVA:3240|Total:20340"
    @ElementCollection
    @CollectionTable(name = "detalle_comprobante", joinColumns = @JoinColumn(name = "comprobante_id"))
    @Column(name = "detalle_pago")
    private List<String> detallePagoPorPersona;

    private double descuento; // descuento total aplicado al grupo (si lo deseas mantener)
    private double precio_final; // precio final del grupo (sin IVA)
    private double iva; // valor del IVA total
    private double monto_total_iva; // precio total con IVA
}
