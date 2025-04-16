package com.example.demo.Entities;

import lombok.*;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "reportes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Reportes {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(unique = true, nullable = false)
    private Long id;

    @ManyToMany
    @JoinTable(
            name = "reporte_comprobante",
            joinColumns = @JoinColumn(name = "reporte_id"),
            inverseJoinColumns = @JoinColumn(name = "comprobante_id")
    )
    private List<Comprobante> comprobantes;
    private LocalDate fechaGeneracion; // Fecha en la que se generó el reporte
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private int total_ingresos;
}
