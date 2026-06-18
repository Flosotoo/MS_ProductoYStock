package com.catalogo.mscatalogo.model;

import java.util.List;

import lombok.Data;

@Data
public class RecepcionProveedorDTO {
    private Long idSucursal;
    private List<ItemRecepcionDTO> items;
}
