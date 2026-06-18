package com.catalogo.mscatalogo.model;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class ProductoStockDTO {
    private String nombre;
    private String descripcion;
    private BigDecimal precio;
    private Categoria categoria;
    private Long idSucursal;
    private int cantidad;
}
