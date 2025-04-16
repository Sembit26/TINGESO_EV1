package com.example.demo.Entities;

import lombok.*;

import jakarta.persistence.*;

@Entity
@Table(name = "karts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Kart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(unique = true, nullable = false)
    private Long id;

    private String modelo;
    private String codificacion;
}
