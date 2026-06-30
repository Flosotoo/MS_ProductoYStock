package com.catalogo.mscatalogo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
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
import org.springframework.web.client.RestTemplate;

import com.catalogo.mscatalogo.exception.RecursoNoEncontradoException;
import com.catalogo.mscatalogo.exception.StockInsuficienteException;
import com.catalogo.mscatalogo.model.EstadoProducto;
import com.catalogo.mscatalogo.model.EstadoStock;
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

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private jakarta.validation.Validator validator;

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
        Inventario inventario = crearInventario(50, 45, 10);
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

    @Test
    void testCancelarReserva_liberaReserva() {
        Inventario inventario = crearInventario(50, 20, 10); // reservada 20
        when(inventarioRepository.findByProducto_IdProductoAndIdSucursal(1L, 1L))
                .thenReturn(Optional.of(inventario));
        when(inventarioRepository.save(any(Inventario.class))).thenAnswer(inv -> inv.getArgument(0));
        Inventario resultado = inventarioService.cancelarReserva(1L, 1L, 5);
        assertEquals(15, resultado.getCantidadReservada()); // 20 - 5
        assertEquals(50, resultado.getCantidad()); // física no cambia
    }

    @Test
    void testCancelarReserva_masDeLoReservado_lanzaExcepcion() {
        Inventario inventario = crearInventario(50, 3, 10); // solo 3 reservadas
        when(inventarioRepository.findByProducto_IdProductoAndIdSucursal(1L, 1L))
                .thenReturn(Optional.of(inventario));
        assertThrows(StockInsuficienteException.class,
                () -> inventarioService.cancelarReserva(1L, 1L, 5));
        verify(inventarioRepository, never()).save(any(Inventario.class));
    }

    @Test
    void testConfirmarReserva_bajaCantidadYReservada() {
        Inventario inventario = crearInventario(50, 20, 10);
        when(inventarioRepository.findByProducto_IdProductoAndIdSucursal(1L, 1L))
                .thenReturn(Optional.of(inventario));
        when(inventarioRepository.save(any(Inventario.class))).thenAnswer(inv -> inv.getArgument(0));
        Inventario resultado = inventarioService.confirmarReserva(1L, 1L, 5);
        // Salida física real: baja cantidad Y reservada
        assertEquals(45, resultado.getCantidad()); // 50 - 5
        assertEquals(15, resultado.getCantidadReservada()); // 20 - 5
    }

    @Test
    void testConfirmarReserva_masDeLoReservado_lanzaExcepcion() {
        Inventario inventario = crearInventario(50, 3, 10);
        when(inventarioRepository.findByProducto_IdProductoAndIdSucursal(1L, 1L))
                .thenReturn(Optional.of(inventario));
        assertThrows(StockInsuficienteException.class,
                () -> inventarioService.confirmarReserva(1L, 1L, 5));
    }

    @Test
    void testApartarStock_cantidadNoPositiva_lanzaExcepcion() {
        assertThrows(StockInsuficienteException.class,
                () -> inventarioService.apartarStock(1L, 1L, 0));
    }

    @Test
    void testAjustarStock_positivo_sumaStock() {
        Inventario inventario = crearInventario(50, 10, 10);
        when(inventarioRepository.findByProducto_IdProductoAndIdSucursal(1L, 1L))
                .thenReturn(Optional.of(inventario));
        when(inventarioRepository.save(any(Inventario.class))).thenAnswer(inv -> inv.getArgument(0));
        Inventario resultado = inventarioService.ajustarStock(1L, 1L, 20, null);
        assertEquals(70, resultado.getCantidad()); // 50 + 20
    }

    @Test
    void testAjustarStock_conIdOperacionYaProcesada_noVuelveAjustar() {
        // Idempotencia: si la operación ya existe, no ajusta de nuevo
        Inventario inventario = crearInventario(50, 10, 10);
        when(operacionStockRepository.existsByIdOperacion("op-1")).thenReturn(true);
        when(inventarioRepository.findByProducto_IdProductoAndIdSucursal(1L, 1L))
                .thenReturn(Optional.of(inventario));
        Inventario resultado = inventarioService.ajustarStock(1L, 1L, 20, "op-1");
        assertEquals(50, resultado.getCantidad()); // NO sumó: sigue en 50
        verify(inventarioRepository, never()).save(any(Inventario.class));
    }

    @Test
    void testConsultarStock_devuelveDTO() {
        Producto producto = new Producto();
        producto.setIdProducto(1L);
        producto.setSku("PER12345");
        producto.setNombre("Perfume Test");
        Inventario inventario = crearInventario(50, 15, 10);
        inventario.setProducto(producto);
        inventario.setEstadoStock(EstadoStock.NORMAL);
        when(inventarioRepository.findByProducto_IdProductoAndIdSucursal(1L, 1L))
                .thenReturn(Optional.of(inventario));
        com.catalogo.mscatalogo.dto.StockConsultaDTO dto = inventarioService.consultarStock(1L, 1L);
        assertEquals("PER12345", dto.getSku());
        assertEquals(35, dto.getCantidadDisponible()); // 50 - 15
    }

    @Test
    void testEliminarInventario_existente() {
        Inventario inventario = crearInventario(50, 0, 10);
        when(inventarioRepository.findById(1L)).thenReturn(Optional.of(inventario));
        inventarioService.eliminarInventario(1L);
        verify(inventarioRepository, times(1)).delete(inventario);
    }

    @Test
    void testEliminarInventario_inexistente_lanzaExcepcion() {
        when(inventarioRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(RecursoNoEncontradoException.class,
                () -> inventarioService.eliminarInventario(99L));
    }

    @Test
    void testGuardarInventario_productoInexistente_lanzaExcepcion() {
        Producto producto = new Producto();
        producto.setIdProducto(99L);
        Inventario inventario = new Inventario();
        inventario.setProducto(producto);
        inventario.setIdSucursal(1L);
        com.catalogo.mscatalogo.dto.SucursalDTO sucursal = new com.catalogo.mscatalogo.dto.SucursalDTO();
        sucursal.setIdSucursal(1L);
        when(restTemplate.getForObject(anyString(), eq(com.catalogo.mscatalogo.dto.SucursalDTO.class)))
                .thenReturn(sucursal);
        when(productoRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(RecursoNoEncontradoException.class,
                () -> inventarioService.guardarInventario(inventario));
    }

    @Test
    void testGuardarInventario_exitoso() {
        Producto producto = new Producto();
        producto.setIdProducto(1L);
        producto.setEstado(EstadoProducto.ACTIVO);
        Inventario inventario = new Inventario();
        inventario.setProducto(producto);
        inventario.setIdSucursal(1L);
        inventario.setCantidad(50);
        inventario.setUmbralMinimo(10);
        // Sucursal válida (mock del RestTemplate)
        com.catalogo.mscatalogo.dto.SucursalDTO sucursal = new com.catalogo.mscatalogo.dto.SucursalDTO();
        sucursal.setIdSucursal(1L);
        when(restTemplate.getForObject(anyString(), eq(com.catalogo.mscatalogo.dto.SucursalDTO.class)))
                .thenReturn(sucursal);
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        when(inventarioRepository.save(any(Inventario.class))).thenAnswer(inv -> inv.getArgument(0));
        Inventario resultado = inventarioService.guardarInventario(inventario);
        assertNotNull(resultado);
        assertEquals(EstadoStock.NORMAL, resultado.getEstadoStock()); // 50 disponible > 10 umbral
    }

    @Test
    void testGuardarInventariosParcial_todosValidos() {
        Producto producto = new Producto();
        producto.setIdProducto(1L);
        producto.setEstado(EstadoProducto.ACTIVO);
        Inventario inv = new Inventario();
        inv.setProducto(producto);
        inv.setIdSucursal(1L);
        inv.setCantidad(50);
        inv.setUmbralMinimo(10);

        // Validador real: no encuentra violaciones en un inventario completo
        when(validator.validate(any(Inventario.class))).thenReturn(java.util.Collections.emptySet());
        com.catalogo.mscatalogo.dto.SucursalDTO sucursal = new com.catalogo.mscatalogo.dto.SucursalDTO();
        sucursal.setIdSucursal(1L);
        when(restTemplate.getForObject(anyString(), eq(com.catalogo.mscatalogo.dto.SucursalDTO.class)))
                .thenReturn(sucursal);
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        when(inventarioRepository.save(any(Inventario.class))).thenAnswer(i -> i.getArgument(0));
        com.catalogo.mscatalogo.dto.InventarioLoteResponse resultado = inventarioService
                .guardarInventariosParcial(java.util.List.of(inv));

        assertEquals(1, resultado.getExitosos().size());
        assertTrue(resultado.getErrores().isEmpty());
    }

    @Test
    void testGuardarInventariosParcial_conErrorDeValidacion() {
        Inventario inv = new Inventario();
        inv.setIdSucursal(1L);
        // Simula una violación de validación
        @SuppressWarnings("unchecked")
        jakarta.validation.ConstraintViolation<Inventario> violacion =
                org.mockito.Mockito.mock(jakarta.validation.ConstraintViolation.class);
        when(validator.validate(any(Inventario.class)))
                .thenReturn(java.util.Set.of(violacion));
        com.catalogo.mscatalogo.dto.InventarioLoteResponse resultado =
                inventarioService.guardarInventariosParcial(java.util.List.of(inv));
        assertTrue(resultado.getExitosos().isEmpty());
        assertEquals(1, resultado.getErrores().size());
    }

    @Test
    void testConsultarStockPorSku_devuelveResultado() {
        Producto producto = new Producto();
        producto.setIdProducto(1L);
        producto.setSku("PER12345");
        producto.setNombre("Perfume Test");
        Inventario inventario = crearInventario(50, 15, 10);
        inventario.setProducto(producto);
        inventario.setEstadoStock(EstadoStock.NORMAL);
        when(productoRepository.findBySku("PER12345")).thenReturn(Optional.of(producto));
        when(inventarioRepository.findByProducto_IdProductoAndIdSucursal(1L, 1L))
                .thenReturn(Optional.of(inventario));
        java.util.List<com.catalogo.mscatalogo.dto.StockConsultaDTO> resultado =
                inventarioService.consultarStockPorSkuONombre("PER12345", null, 1L);
        assertEquals(1, resultado.size());
        assertEquals(35, resultado.get(0).getCantidadDisponible());
    }

    @Test
    void testConsultarStockPorSku_productoNoExiste_lanzaExcepcion() {
        when(productoRepository.findBySku("XXX")).thenReturn(Optional.empty());
        assertThrows(RecursoNoEncontradoException.class,
                () -> inventarioService.consultarStockPorSkuONombre("XXX", null, 1L));
    }

    @Test
    void testGuardarInventario_productoNull_lanzaExcepcion() {
        Inventario inventario = new Inventario();
        inventario.setProducto(null); // sin producto
        inventario.setIdSucursal(1L);
        assertThrows(RecursoNoEncontradoException.class,
                () -> inventarioService.guardarInventario(inventario));
    }

}
