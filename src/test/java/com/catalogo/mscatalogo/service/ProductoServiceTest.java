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
}
