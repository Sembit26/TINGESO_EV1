package com.example.demo.Entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "reservas")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Reserva {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(unique = true, nullable = false)
    public Long id;

    @ManyToOne
    @JoinColumn(name = "cliente_id", nullable = false)
    public Client cliente;

    @ManyToMany
    @JoinTable(
            name = "reserva_karts",
            joinColumns = @JoinColumn(name = "reserva_id"),
            inverseJoinColumns = @JoinColumn(name = "kart_id")
    )
    public List<Kart> kartsAsignados;

    @ElementCollection
    @CollectionTable(name = "personas_reserva", joinColumns = @JoinColumn(name = "reserva_id"))
    @Column(name = "persona")
    private List<String> personasReserva;  // Almacenar nombre y correo como String separados por un delimitador (name1,correo1)

    public int num_vueltas_tiempo_maximo;
    public int num_personas; //Cantidad de personas para las que se generó la reserva
    public int precio_regular;
    public int duracion_total;
    public LocalDateTime fechaHora; // Fecha en la que se generó la reserva
}
