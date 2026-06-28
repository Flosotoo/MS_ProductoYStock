package com.catalogo.mscatalogo.dto;

import java.util.List;

import com.catalogo.mscatalogo.model.Inventario;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class InventarioLoteRequest {
    @NotEmpty(message = "Debe enviar al menos un inventario")
    private List<Inventario> inventarios;
}
