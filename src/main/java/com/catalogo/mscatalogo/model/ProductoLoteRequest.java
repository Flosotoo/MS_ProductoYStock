package com.catalogo.mscatalogo.model;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class ProductoLoteRequest {
    @NotEmpty(message = "Debe enviar al menos un producto")
    private List<Producto> productos;
}
