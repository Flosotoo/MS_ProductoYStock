package com.catalogo.mscatalogo.controller;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.catalogo.mscatalogo.exception.RecursoDuplicadoException;
import com.catalogo.mscatalogo.model.Categoria;
import com.catalogo.mscatalogo.model.EstadoProducto;
import com.catalogo.mscatalogo.model.Producto;
import com.catalogo.mscatalogo.service.ProductoService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(ProductoController.class)
class ProductoControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductoService productoService;

    private ObjectMapper objectMapper = new ObjectMapper();

    private Producto crearProducto() {
        Producto p = new Producto();
        p.setIdProducto(1L);
        p.setSku("PER12345");
        p.setNombre("Perfume Test");
        p.setPrecio(new BigDecimal("45000"));
        p.setDescripcion("Desc");
        p.setCategoria(Categoria.PERFUME);
        p.setEstado(EstadoProducto.ACTIVO);
        return p;
    }

    @Test
    void testPostProducto_devuelve201() throws Exception {
        Producto p = crearProducto();
        when(productoService.guardarProducto(any(Producto.class))).thenReturn(p);
        mockMvc.perform(post("/api/productos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(p)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sku").value("PER12345"));
    }

    @Test
    void testPostProducto_skuDuplicado_devuelve409() throws Exception {
        Producto p = crearProducto();
        when(productoService.guardarProducto(any(Producto.class)))
                .thenThrow(new RecursoDuplicadoException("Ya existe un producto con el SKU"));
        mockMvc.perform(post("/api/productos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(p)))
                .andExpect(status().isConflict());
    }

    @Test
    void testGetProductos_devuelve200() throws Exception {
        when(productoService.listarProductos()).thenReturn(List.of(crearProducto()));
        mockMvc.perform(get("/api/productos"))
                .andExpect(status().isOk());
    }

    @Test
    void testGetProductos_vacio_devuelve204() throws Exception {
        when(productoService.listarProductos()).thenReturn(List.of());
        mockMvc.perform(get("/api/productos"))
                .andExpect(status().isNoContent());
    }

    @Test
    void testBuscarPorCategoria_devuelve200() throws Exception {
        when(productoService.buscarPorCategoria(Categoria.PERFUME)).thenReturn(List.of(crearProducto()));
        mockMvc.perform(get("/api/productos/categoria/PERFUME"))
                .andExpect(status().isOk());
    }

    @Test
    void testEliminarProducto_devuelve204() throws Exception {
        when(productoService.findById(1L)).thenReturn(Optional.of(crearProducto()));
        doNothing().when(productoService).eliminarProducto(1L);
        mockMvc.perform(delete("/api/productos/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void testEliminarProducto_inexistente_devuelve404() throws Exception {
        when(productoService.findById(9999L)).thenReturn(Optional.empty());
        mockMvc.perform(delete("/api/productos/9999"))
                .andExpect(status().isNotFound());
    }
}
