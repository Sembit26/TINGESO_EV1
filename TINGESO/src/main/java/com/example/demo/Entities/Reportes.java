package com.example.demo.Entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "reportes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Reportes {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(unique = true, nullable = false)
    public Long id;

    @ManyToMany
    @JoinTable(
            name = "reporte_comprobante",
            joinColumns = @JoinColumn(name = "reporte_id"),
            inverseJoinColumns = @JoinColumn(name = "comprobante_id")
    )
    public List<Comprobante> comprobantes;

    public LocalDate fechaGeneracion; // Fecha en la que se generó el reporte
    public LocalDate fechaInicio;
    public LocalDate fechaFin;
    public int total_ingresos;
}
