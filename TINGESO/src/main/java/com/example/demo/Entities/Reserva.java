package com.example.demo.Entities;

import lombok.*;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Entity
@Table(name = "reservas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Reserva {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(unique = true, nullable = false)
    private Long id;

    @ManyToMany
    @JoinTable(
            name = "reserva_karts",
            joinColumns = @JoinColumn(name = "reserva_id"),
            inverseJoinColumns = @JoinColumn(name = "kart_id")
    )
    private List<Kart> kartsAsignados;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "comprobante_id")
    private Comprobante comprobante;

    private int num_vueltas_tiempo_maximo;
    private int num_personas; //Cantidad de personas para las que se generó la reserva
    private int precio_regular;
    private int duracion_total;
    private LocalDateTime fechaHora; // Fecha en la que se generó la reserva
    private String nombreCliente;

    private LocalDate fechaInicio; //fecha de inicio de reserva
    private LocalTime horaInicio; // hora de inicio de reserva
    private LocalTime horaFin; // hora de fin de la reserva
}
