package com.catalogo.mscatalogo.model;

import java.util.List;

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
