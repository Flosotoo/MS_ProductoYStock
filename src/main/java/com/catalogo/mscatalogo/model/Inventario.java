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
    private Long id;
    private Producto producto;
    private Long sucursalId;
    private int stockActual = 0;
    private int stockMinimo = 5;
    private LocalDateTime fechaActualizacion;

}
