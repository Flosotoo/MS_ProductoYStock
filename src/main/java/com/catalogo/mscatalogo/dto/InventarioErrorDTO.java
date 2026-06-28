package com.catalogo.mscatalogo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventarioErrorDTO {
    private int posicion;       // índice del elemento en la lista enviada (0-based)
    private String idProducto;  // referencia para identificar qué item falló
    private Long idSucursal;
    private String mensaje;

}
