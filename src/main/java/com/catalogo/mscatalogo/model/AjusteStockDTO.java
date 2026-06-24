package com.catalogo.mscatalogo.model;

import lombok.Data;

@Data
public class AjusteStockDTO {
    private Long idProducto;
    private Long idSucursal;
    private int cantidad;
    private String idOperacion;

}
