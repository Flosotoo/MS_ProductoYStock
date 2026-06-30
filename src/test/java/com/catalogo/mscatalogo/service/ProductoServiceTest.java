package com.catalogo.mscatalogo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.catalogo.mscatalogo.exception.RecursoDuplicadoException;
import com.catalogo.mscatalogo.model.Categoria;
import com.catalogo.mscatalogo.model.EstadoProducto;
import com.catalogo.mscatalogo.model.Producto;
import com.catalogo.mscatalogo.repository.ProductoRepository;

@ExtendWith(MockitoExtension.class)
public class ProductoServiceTest {
    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private jakarta.validation.Validator validator;

    @InjectMocks
    private ProductoService productoService;

    private Producto crearProducto(String sku) {
        Producto producto = new Producto();
        producto.setSku(sku);
        producto.setNombre("Perfume Test");
        producto.setPrecio(new BigDecimal("45000"));
        producto.setDescripcion("Desc");
        producto.setCategoria(Categoria.PERFUME);
        return producto;
    }

    @Test
    void testGuardarProducto_skuNulo_generaSkuYGuarda() {
        // Sin SKU se genera automaticamente y guarda con estado activo por defecto
        Producto producto = crearProducto(null);
        when(productoRepository.existsBySku(any())).thenReturn(false);
        when(productoRepository.save(any(Producto.class))).thenAnswer(inv -> inv.getArgument(0));
        Producto resultado = productoService.guardarProducto(producto);
        assertTrue(resultado.getSku() != null && resultado.getSku().startsWith("PER"));
        assertEquals(EstadoProducto.ACTIVO, resultado.getEstado());
        verify(productoRepository, times(1)).save(any(Producto.class));
    }

    @Test
    void testGuardarProducto_skuDuplicado_lanzaExcepcion() {
        // Con SKU ya existente, el recurso se duplica y lanza Exception
        Producto producto = crearProducto("PER12345");
        when(productoRepository.existsBySku("PER12345")).thenReturn(true);
        RecursoDuplicadoException ex = assertThrows(
                RecursoDuplicadoException.class,
                () -> productoService.guardarProducto(producto));
        assertTrue(ex.getMessage().contains("SKU"));
        verify(productoRepository, never()).save(any(Producto.class));
    }

    @Test
    void testListarProductos() {
        when(productoRepository.findAll()).thenReturn(java.util.List.of(crearProducto(null), crearProducto(null)));
        java.util.List<Producto> resultado = productoService.listarProductos();
        assertEquals(2, resultado.size());
    }

    @Test
    void testBuscarPorNombre() {
        when(productoRepository.findByNombreContainingIgnoreCase("perfume"))
                .thenReturn(java.util.List.of(crearProducto(null)));

        java.util.List<Producto> resultado = productoService.buscarPorNombre("perfume");
        assertEquals(1, resultado.size());
    }

    @Test
    void testBuscarPorCategoria() {
        when(productoRepository.findByCategoria(Categoria.PERFUME))
                .thenReturn(java.util.List.of(crearProducto(null)));
        java.util.List<Producto> resultado = productoService.buscarPorCategoria(Categoria.PERFUME);
        assertEquals(1, resultado.size());
    }

    @Test
    void testFindById_existente() {
        Producto producto = crearProducto("PER12345");
        when(productoRepository.findById(1L)).thenReturn(java.util.Optional.of(producto));
        java.util.Optional<Producto> resultado = productoService.findById(1L);
        assertTrue(resultado.isPresent());
    }

    @Test
    void testEliminarProducto() {
        productoService.eliminarProducto(1L);
        verify(productoRepository, times(1)).deleteById(1L);
    }

    @Test
    void testActualizarProducto_estadoNull_asignaActivo() {
        Producto producto = crearProducto("PER12345");
        producto.setEstado(null);
        when(productoRepository.save(any(Producto.class))).thenAnswer(inv -> inv.getArgument(0));
        Producto resultado = productoService.actualizarProducto(producto);
        assertEquals(EstadoProducto.ACTIVO, resultado.getEstado());
    }

    @Test
    void testGuardarProducto_skuValidoNuevo_guarda() {
        // SKU provisto y no duplicado: se guarda tal cual
        Producto producto = crearProducto("PER99999");
        when(productoRepository.existsBySku("PER99999")).thenReturn(false);
        when(productoRepository.save(any(Producto.class))).thenAnswer(inv -> inv.getArgument(0));
        Producto resultado = productoService.guardarProducto(producto);
        assertEquals("PER99999", resultado.getSku());
        assertEquals(EstadoProducto.ACTIVO, resultado.getEstado());
    }

    @Test
    void testGuardarProductosParcial_todosValidos() {
        Producto p = crearProducto(null); // sin SKU, se autogenera

        when(validator.validate(any(Producto.class))).thenReturn(java.util.Collections.emptySet());
        when(productoRepository.existsBySku(any())).thenReturn(false);
        when(productoRepository.save(any(Producto.class))).thenAnswer(i -> i.getArgument(0));

        com.catalogo.mscatalogo.dto.ProductoLoteResponse resultado = productoService
                .guardarProductosParcial(java.util.List.of(p));

        assertEquals(1, resultado.getExitosos().size());
        assertTrue(resultado.getErrores().isEmpty());
    }

    @Test
    void testGuardarProductosParcial_conErrorValidacion() {
        Producto p = new Producto(); // incompleto
        @SuppressWarnings("unchecked")
        jakarta.validation.ConstraintViolation<Producto> violacion = org.mockito.Mockito
                .mock(jakarta.validation.ConstraintViolation.class);
        when(validator.validate(any(Producto.class)))
                .thenReturn(java.util.Set.of(violacion));
        com.catalogo.mscatalogo.dto.ProductoLoteResponse resultado = productoService
                .guardarProductosParcial(java.util.List.of(p));
        assertTrue(resultado.getExitosos().isEmpty());
        assertEquals(1, resultado.getErrores().size());
    }

    @Test
    void testGuardarProductosParcial_skuDuplicado_vaAErrores() {
        Producto p = crearProducto("PER12345");
        when(validator.validate(any(Producto.class))).thenReturn(java.util.Collections.emptySet());
        when(productoRepository.existsBySku("PER12345")).thenReturn(true); // duplicado

        com.catalogo.mscatalogo.dto.ProductoLoteResponse resultado = productoService
                .guardarProductosParcial(java.util.List.of(p));
        assertEquals(1, resultado.getErrores().size());
        assertTrue(resultado.getExitosos().isEmpty());
    }
}
