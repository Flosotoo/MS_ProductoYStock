package com.catalogo.mscatalogo.model;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity
@Table(name = "inventario", uniqueConstraints = {@UniqueConstraint(name = "uk_inventario_producto_sucursal", columnNames = {"producto_id", "sucursal_id"})})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Inventario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "El producto no puede ser nulo")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @NotNull(message = "El ID de la sucursal no puede ser nula")
    @Positive(message = "El ID de la sucursal debe ser un número positivo")
    @Column(name = "sucursal_id", nullable = false)
    private Long sucursalId;

    @Min(value = 0, message = "El stock actual no puede ser negativo")
    @Column(nullable = false)
    @Builder.Default
    private int stockActual = 0;

    @Min(value = 0, message = "El stock mínimo no puede ser negativo")
    @Column(nullable = false)
    @Builder.Default
    private int stockMinimo = 5;

    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion;

    @PrePersist
    @PreUpdate
    public void actualizarFecha() {
        this.fechaActualizacion = LocalDateTime.now();
    }

}
