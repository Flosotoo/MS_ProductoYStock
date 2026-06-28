package com.catalogo.mscatalogo.dto;

import java.util.List;

import com.catalogo.mscatalogo.model.Producto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductoLoteResponse {
    private List<Producto> exitosos;
    private List<ProductoErrorDTO> errores;
}
