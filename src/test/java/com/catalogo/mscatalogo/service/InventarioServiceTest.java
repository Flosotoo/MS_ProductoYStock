package com.catalogo.mscatalogo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.catalogo.mscatalogo.exception.RecursoNoEncontradoException;
import com.catalogo.mscatalogo.exception.StockInsuficienteException;
import com.catalogo.mscatalogo.model.EstadoProducto;
import com.catalogo.mscatalogo.model.Inventario;
import com.catalogo.mscatalogo.model.Producto;
import com.catalogo.mscatalogo.repository.InventarioRepository;
import com.catalogo.mscatalogo.repository.OperacionStockRepository;
import com.catalogo.mscatalogo.repository.ProductoRepository;

@ExtendWith(MockitoExtension.class)
class InventarioServiceTest {
    @Mock
    private InventarioRepository inventarioRepository;

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private OperacionStockRepository operacionStockRepository;

    @InjectMocks
    private InventarioService inventarioService;

    private Inventario crearInventario(int cantidad, int reservada, int umbral) {
        Producto producto = new Producto();
        producto.setIdProducto(1L);
        producto.setEstado(EstadoProducto.ACTIVO);
        Inventario inventario = new Inventario();
        inventario.setIdInventario(1L);
        inventario.setProducto(producto);
        inventario.setIdSucursal(1L);
        inventario.setCantidad(cantidad);
        inventario.setCantidadReservada(reservada);
        inventario.setUmbralMinimo(umbral);
        return inventario;
    }

    @Test
    void testApartarStock_descuentaDelDisponible() {
        // cantidad 50, reservada 0 -> apartar 10 -> reservada 10, disponible 40
        Inventario inventario = crearInventario(50, 0, 10);
        when(inventarioRepository.findByProducto_IdProductoAndIdSucursal(1L, 1L))
                .thenReturn(Optional.of(inventario));
        when(inventarioRepository.save(any(Inventario.class))).thenAnswer(inv -> inv.getArgument(0));
        Inventario resultado = inventarioService.apartarStock(1L, 1L, 10);
        assertEquals(10, resultado.getCantidadReservada());
        assertEquals(50, resultado.getCantidad()); // la cantidad física no cambia
        verify(inventarioRepository, times(1)).save(any(Inventario.class));
    }

    @Test
    void testApartarStock_sinDisponibleSuficiente_lanzaExcepcion() {
        // cantidad 10, reservada 8 => disponible 2 => apartar 5 falla
        Inventario inventario = crearInventario(10, 8, 5);
        when(inventarioRepository.findByProducto_IdProductoAndIdSucursal(1L, 1L))
                .thenReturn(Optional.of(inventario));
        StockInsuficienteException ex = assertThrows(
                StockInsuficienteException.class,
                () -> inventarioService.apartarStock(1L, 1L, 5));
        assertTrue(ex.getMessage().contains("Stock insuficiente"));
        verify(inventarioRepository, never()).save(any(Inventario.class));
    }

    @Test
    void testAjustarStock_dejariaBajoLoReservado_lanzaExcepcion() {
        // cantidad 50, reservada 45 => ajustar -10 dejaría 40 < 45 reservadas => falla
        Inventario inventario = crearInventario(50, 45, 10);
        when(operacionStockRepository.existsByIdOperacion(any())).thenReturn(false);
        when(inventarioRepository.findByProducto_IdProductoAndIdSucursal(1L, 1L))
                .thenReturn(Optional.of(inventario));
        StockInsuficienteException ex = assertThrows(
                StockInsuficienteException.class,
                () -> inventarioService.ajustarStock(1L, 1L, -10, null));
        assertTrue(ex.getMessage().contains("reservadas"));
        verify(inventarioRepository, never()).save(any(Inventario.class));
    }

    @Test
    void testVerificarDisponibilidad_devuelveCantidadMenosReservada() {
        // cantidad 50, reservada 15 => disponible 35
        Inventario inventario = crearInventario(50, 15, 10);
        when(inventarioRepository.findByProducto_IdProductoAndIdSucursal(1L, 1L))
                .thenReturn(Optional.of(inventario));
        int disponible = inventarioService.verificarDisponibilidad(1L, 1L);
        assertEquals(35, disponible);
    }

    @Test
    void testVerificarDisponibilidad_inventarioInexistente_lanzaExcepcion() {
        when(inventarioRepository.findByProducto_IdProductoAndIdSucursal(99L, 1L))
                .thenReturn(Optional.empty());
        assertThrows(RecursoNoEncontradoException.class,
                () -> inventarioService.verificarDisponibilidad(99L, 1L));
    }
}
