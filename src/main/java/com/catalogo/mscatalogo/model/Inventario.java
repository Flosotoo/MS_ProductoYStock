package com.catalogo.mscatalogo.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
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

    @NotNull(message = "El producto es obligatorio")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_producto", nullable = false, foreignKey = @ForeignKey(name = "fk_inventario_producto"))
    private Producto producto;

    @NotNull(message = "La sucursal es obligatoria")
    @Positive(message = "El id de sucursal debe ser un número positivo")
    @Column(name = "id_sucursal", nullable = false)
    private Long idSucursal;

    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 0, message = "La cantidad no puede ser negativa")
    @Column(nullable = false)
    private Integer cantidad;

    @Min(value = 0, message = "La cantidad reservada no puede ser negativa")
    @Column(nullable = false)
    private Integer cantidadReservada = 0;

    @NotNull(message = "El umbral mínimo es obligatorio")
    @Min(value = 0, message = "El umbral mínimo no puede ser negativo")
    @Column(nullable = false)
    private Integer umbralMinimo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoStock estadoStock;
}
