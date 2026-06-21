package com.catalogo.mscatalogo.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventarioLoteResponse {
    private List<Inventario> exitosos;
    private List<InventarioErrorDTO> errores;
}
