package com.catalogo.mscatalogo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor

public class Producto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idProducto;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, length = 100)
    private String marca;   

    @Column(nullable = true, length = 500)
    private String descripcion;

    @Column(nullable = false)
    private Double precio;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Categoria categoria;

    public Producto(Categoria categoria, String descripcion, Long idProducto, String marca, String nombre, Double precio) {
        this.categoria = categoria;
        this.descripcion = descripcion;
        this.idProducto = idProducto;
        this.marca = marca;
        this.nombre = nombre;
        this.precio = precio;
    }
}
