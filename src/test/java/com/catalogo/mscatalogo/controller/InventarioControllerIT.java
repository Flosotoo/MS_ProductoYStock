package com.catalogo.mscatalogo.controller;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;

import com.catalogo.mscatalogo.dto.SucursalDTO;
import com.catalogo.mscatalogo.model.Categoria;
import com.catalogo.mscatalogo.model.Inventario;
import com.catalogo.mscatalogo.model.Producto;
import com.catalogo.mscatalogo.repository.InventarioRepository;
import com.catalogo.mscatalogo.repository.ProductoRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.catalogo.mscatalogo.model.EstadoProducto;


@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class InventarioControllerIT {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private InventarioRepository inventarioRepository;

    @MockitoBean
    private RestTemplate restTemplate;

    private ObjectMapper objectMapper = new ObjectMapper();

    private Long idProducto;

    @BeforeEach
    void setUp() {
        inventarioRepository.deleteAll();
        productoRepository.deleteAll();
        Producto producto = new Producto();
        producto.setSku("PER12345");
        producto.setNombre("Perfume Test");
        producto.setPrecio(new BigDecimal("45000"));
        producto.setDescripcion("Desc test");
        producto.setCategoria(Categoria.PERFUME);
        producto.setEstado(EstadoProducto.ACTIVO);
        idProducto = productoRepository.save(producto).getIdProducto();
        // La sucursal existe
        SucursalDTO sucursal = new SucursalDTO();
        sucursal.setIdSucursal(1L);
        sucursal.setNombre("Sucursal Centro");
        when(restTemplate.getForObject(anyString(), eq(SucursalDTO.class))).thenReturn(sucursal);
    }

    @Test
    void testCrearInventario_devuelve201() throws Exception {
        Producto refProducto = new Producto();
        refProducto.setIdProducto(idProducto);
        Inventario inventario = new Inventario();
        inventario.setProducto(refProducto);
        inventario.setIdSucursal(1L);
        inventario.setCantidad(50);
        inventario.setUmbralMinimo(10);
        mockMvc.perform(post("/api/inventario")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(inventario)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idInventario").exists())
                .andExpect(jsonPath("$.cantidad").value(50))
                .andExpect(jsonPath("$.estadoStock").value("NORMAL"));
    }

    @Test
    void testGetInventario_inexistente_devuelve404() throws Exception {
        mockMvc.perform(get("/api/inventario/9999"))
                .andExpect(status().isNotFound());
    }
}
