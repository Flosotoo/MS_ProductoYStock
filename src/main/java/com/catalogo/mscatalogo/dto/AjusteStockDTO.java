package com.catalogo.mscatalogo.dto;

import lombok.Data;

@Data
public class AjusteStockDTO {
    private Long idProducto;
    private Long idSucursal;
    private int cantidad;
    private String idOperacion;

}
