package com.catalogo.mscatalogo.model;

import lombok.Data;

@Data
public class StockOperacionDTO {
    private String skuProducto;
    private Long idSucursal;
    private int cantidad;
}
