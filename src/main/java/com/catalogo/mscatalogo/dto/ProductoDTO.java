package com.catalogo.mscatalogo.dto;

import java.math.BigDecimal;

import com.catalogo.mscatalogo.model.Categoria;
import com.catalogo.mscatalogo.model.EstadoProducto;

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
