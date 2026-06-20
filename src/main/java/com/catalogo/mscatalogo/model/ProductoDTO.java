package com.catalogo.mscatalogo.model;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class ProductoDTO {
    private Long idProducto;
    private String sku;
    private String nombre;
    private BigDecimal precio;
    private String descripcion;
    private Categoria categoria;
    private EstadoProducto estado;
}
