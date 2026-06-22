package com.catalogo.mscatalogo.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductoErrorDTO {
    private int posicion;       
    private String sku;   
    private String nombre;
    private String mensaje;

}
