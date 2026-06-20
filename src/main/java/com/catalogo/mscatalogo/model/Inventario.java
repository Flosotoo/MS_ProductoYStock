package com.catalogo.mscatalogo.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "inventario", uniqueConstraints = {
        @UniqueConstraint(name = "uk_inventario_producto_sucursal", columnNames = { "id_producto", "id_sucursal" }) })
@Data
@NoArgsConstructor
@AllArgsConstructor

public class Inventario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idInventario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_producto", nullable = false, foreignKey = @ForeignKey(name = "fk_inventario_producto"))
    private Producto producto;

    @Column(name = "id_sucursal", nullable = false)
    private Long idSucursal;

    @Column(nullable = false)
    private int cantidad;

    @Column(nullable = false)
    private int umbralMinimo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoStock estadoStock;
}
