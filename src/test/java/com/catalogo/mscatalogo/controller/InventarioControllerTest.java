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
}
