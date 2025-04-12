package com.example.demo.Entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "clients")
@Data // Esto genera automáticamente los getters y setters para todos los campos
@NoArgsConstructor
@AllArgsConstructor
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(unique = true, nullable = false)
    public Long id;

    public String rut;
    public String name;
    public String email;
    public String contrasena;
    public LocalDate birthday;
    public int num_visitas_al_mes;


}
