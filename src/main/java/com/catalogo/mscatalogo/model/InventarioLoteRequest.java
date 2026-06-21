package com.catalogo.mscatalogo.model;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class InventarioLoteRequest {
    @NotEmpty(message = "Debe enviar al menos un inventario")
    private List<Inventario> inventarios;
}
