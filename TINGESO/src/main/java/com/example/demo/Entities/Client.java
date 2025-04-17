package com.example.demo.Entities;

import lombok.*;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "clients")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(unique = true, nullable = false)
    private Long id;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "cliente_id") // Se usa esta columna en la tabla reserva
    private List<Reserva> reservas;

    private String rut;
    private String name;
    private String email;
    private String contrasena;
    private LocalDate birthday;
    private int num_visitas_al_mes;

    private LocalDate lastLoginDate; //ultimo mes que se conecto el cliente


}
