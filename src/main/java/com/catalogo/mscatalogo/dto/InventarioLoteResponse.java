package com.catalogo.mscatalogo.dto;

import java.util.List;

import com.catalogo.mscatalogo.model.Inventario;

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
