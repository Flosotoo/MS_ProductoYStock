package com.catalogo.mscatalogo.model;

public enum Categoria {
    PERFUME("PRF"),
    COLONIA("COL"),
    BODY_SPLASH("BSP"),
    CUIDADO_PERSONAL("CPS");

    private final String prefijuSku;
    Categoria(String prefijuSku) {
        this.prefijuSku = prefijuSku;
    }
    public String getPrefijuSku() {
        return prefijuSku;
    }
}
