package com.catalogo.mscatalogo.model;

import java.math.BigDecimal;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "producto", uniqueConstraints = { @UniqueConstraint(name = "uk_producto_sku", columnNames = { "sku" }) })
@Data
@NoArgsConstructor
@AllArgsConstructor

public class Producto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idProducto;

    @Column(nullable = false, length = 8, unique = true)
    private String sku;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(precision = 10, scale = 0, nullable = false)
    private BigDecimal precio;

    @Column(nullable = false, length = 250)
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Categoria categoria;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoProducto estado;

}
