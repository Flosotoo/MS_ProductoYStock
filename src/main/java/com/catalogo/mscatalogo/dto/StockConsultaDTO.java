package com.catalogo.mscatalogo.dto;

import com.catalogo.mscatalogo.model.EstadoStock;

import lombok.Data;

@Data
public class StockConsultaDTO {
    private String sku;
    private String nombre;
    private int cantidadDisponible;
    private int umbralMinimo;
    private EstadoStock estadoStock;
}
