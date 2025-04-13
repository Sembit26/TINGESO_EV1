package com.example.demo.Entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "comprobantes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Comprobante {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(unique = true, nullable = false)
    public Long id;

    @OneToOne
    @JoinColumn(name = "reserva_id", nullable = false)
    public Reserva reserva;

    @ElementCollection
    @CollectionTable(name = "comprobante_pagos", joinColumns = @JoinColumn(name = "comprobante_id"))
    @Column(name = "detalle_pago")
    public List<String> pagosPorPersona; // Ej: ["Juan:11200", "Pedro:11200", "Ana:8400"]

    public double descuento; //descuento por num de personas, cumpleanos, etc
    public double precio_final; //precio final
    public double iva; //valor del iva
    public double monto_total_iva;
}
