package com.catalogo.mscatalogo.controller;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.catalogo.mscatalogo.exception.RecursoNoEncontradoException;
import com.catalogo.mscatalogo.service.InventarioService;

@WebMvcTest(InventarioController.class)
class InventarioControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private InventarioService inventarioService;

    @Test
    void testVerificarDisponibilidad_devuelve200YElNumero() throws Exception {
        when(inventarioService.verificarDisponibilidad(1L, 1L)).thenReturn(35);
        mockMvc.perform(get("/api/inventario/disponibilidad")
                .param("idProducto", "1")
                .param("idSucursal", "1"))
                .andExpect(status().isOk())
                .andExpect(content().string("35"));
    }

    @Test
    void testGetInventarioPorId_inexistente_devuelve404() throws Exception {
        when(inventarioService.findById(9999L)).thenReturn(java.util.Optional.empty());
        mockMvc.perform(get("/api/inventario/9999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testVerificarDisponibilidad_inventarioInexistente_devuelve404() throws Exception {
        when(inventarioService.verificarDisponibilidad(anyLong(), anyLong()))
                .thenThrow(new RecursoNoEncontradoException("No existe inventario"));
        mockMvc.perform(get("/api/inventario/disponibilidad")
                .param("idProducto", "99")
                .param("idSucursal", "1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetInventario_devuelve200() throws Exception {
        com.catalogo.mscatalogo.model.Inventario inv = new com.catalogo.mscatalogo.model.Inventario();
        inv.setIdInventario(1L);
        when(inventarioService.listarInventario()).thenReturn(java.util.List.of(inv));
        mockMvc.perform(get("/api/inventario"))
                .andExpect(status().isOk());
    }

    @Test
    void testGetInventario_vacio_devuelve204() throws Exception {
        when(inventarioService.listarInventario()).thenReturn(java.util.List.of());
        mockMvc.perform(get("/api/inventario"))
                .andExpect(status().isNoContent());
    }

    @Test
    void testApartarStock_stockInsuficiente_devuelve409() throws Exception {
        when(inventarioService.apartarStock(anyLong(), anyLong(), org.mockito.ArgumentMatchers.anyInt()))
                .thenThrow(new com.catalogo.mscatalogo.exception.StockInsuficienteException("Stock insuficiente"));
        String body = "{ \"idProducto\": 1, \"idSucursal\": 1, \"cantidad\": 5 }";
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/inventario/apartar")
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isConflict());
    }

    @Test
    void testAjustarStock_devuelve200() throws Exception {
        com.catalogo.mscatalogo.model.Inventario inv = new com.catalogo.mscatalogo.model.Inventario();
        inv.setIdInventario(1L);
        when(inventarioService.ajustarStock(anyLong(), anyLong(), org.mockito.ArgumentMatchers.anyInt(), org.mockito.ArgumentMatchers.any()))
                .thenReturn(inv);
        String body = "{ \"idProducto\": 1, \"idSucursal\": 1, \"cantidad\": 20 }";
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/inventario/ajustar")
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isOk());
    }
}
