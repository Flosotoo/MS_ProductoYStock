package com.catalogo.mscatalogo.model;

import lombok.Data;

@Data
public class StockOperacionResponseDTO {
    private boolean exitoso;
    private String skuProducto;
    private int cantidadResultante;
    private EstadoProducto estadoProducto;
    private String mensaje;
}
