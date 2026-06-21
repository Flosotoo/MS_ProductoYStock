package com.catalogo.mscatalogo.model;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Entity
@Table(name = "producto", uniqueConstraints = { @UniqueConstraint(name = "uk_producto_sku", columnNames = { "sku" }) })
@Data
@NoArgsConstructor
@AllArgsConstructor

public class Producto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idProducto;

    @NotBlank(message = "El SKU es obligatorio")
    @Column(nullable = false, length = 8, unique = true)
    private String sku;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre no puede exceder los 100 caracteres")
    @Column(nullable = false, length = 100)
    private String nombre;

    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.0", inclusive = false, message = "El precio debe ser mayor que cero")
    @Digits(integer = 10, fraction = 0, message = "El precio debe ser un número entero con hasta 10 dígitos")
    @Column(precision = 10, scale = 0, nullable = false)
    private BigDecimal precio;

    @NotBlank(message = "La descripción es obligatoria")
    @Size(max = 250, message = "La descripción no puede exceder los 250 caracteres")
    @Column(nullable = false, length = 250)
    private String descripcion;

    @NotNull(message = "La categoria es obligatoria")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Categoria categoria;

    @NotNull(message = "El estado es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoProducto estado;

}
