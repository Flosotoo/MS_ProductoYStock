package com.catalogo.mscatalogo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventarioErrorDTO {
    private int posicion;      
    private String idProducto;  
    private Long idSucursal;
    private String mensaje;

}
