package com.catalogo.mscatalogo.dto;

import java.util.List;

import com.catalogo.mscatalogo.model.Producto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class ProductoLoteRequest {
    @NotEmpty(message = "Debe enviar al menos un producto")
    private List<Producto> productos;
}
