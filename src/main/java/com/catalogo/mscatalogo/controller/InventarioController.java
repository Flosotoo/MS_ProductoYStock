package com.catalogo.mscatalogo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.catalogo.mscatalogo.dto.AjusteStockDTO;
import com.catalogo.mscatalogo.dto.InventarioLoteRequest;
import com.catalogo.mscatalogo.dto.InventarioLoteResponse;
import com.catalogo.mscatalogo.dto.StockConsultaDTO;
import com.catalogo.mscatalogo.exception.RecursoNoEncontradoException;
import com.catalogo.mscatalogo.model.Inventario;
import com.catalogo.mscatalogo.service.InventarioService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/inventario")
@Tag(name = "Inventario", description = "Gestión de stock, reservas y disponibilidad (HU-12, HU-13, HU-15, HU-51)")

public class InventarioController {
    @Autowired
    private InventarioService inventarioService;

    @Operation(summary = "Listar todo el inventario", description = "Devuelve el catálogo completo de inventario. 204 si está vacío.")
    @GetMapping
    public ResponseEntity<List<Inventario>> getInventario() {
        List<Inventario> inventarios = inventarioService.listarInventario();
        if (inventarios.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(inventarios, HttpStatus.OK);
    }

    @Operation(summary = "Crear inventario", description = "Registra stock de un producto en una sucursal. Valida la sucursal vía MS Sucursales.")
    @PostMapping
    public ResponseEntity<Inventario> postInventario(@Valid @RequestBody Inventario inventario) {
        Inventario nuevo = inventarioService.guardarInventario(inventario);
        return new ResponseEntity<>(nuevo, HttpStatus.CREATED);
    }

    @Operation(summary = "Crear inventario en lote", description = "Registra stock de múltiples productos en una sucursal. Valida la sucursal vía MS Sucursales.")
    @PostMapping("/lote-parcial")
    public ResponseEntity<InventarioLoteResponse> postInventarioLoteParcial(
            @Valid @RequestBody InventarioLoteRequest request) {
        InventarioLoteResponse resultado = inventarioService.guardarInventariosParcial(request.getInventarios());
        if (resultado.getErrores().isEmpty()) {
            return new ResponseEntity<>(resultado, HttpStatus.CREATED); // 201: todo OK
        } else if (resultado.getExitosos().isEmpty()) {
            return new ResponseEntity<>(resultado, HttpStatus.BAD_REQUEST); // 400: nada se guardó
        } else {
            return new ResponseEntity<>(resultado, HttpStatus.MULTI_STATUS); // 207: éxito parcial
        }
    }

    @Operation(summary = "Obtener inventario por ID", description = "Devuelve los detalles de un registro de inventario específico. Devuelve 404 si no existe.")
    @GetMapping("/{id}")
    public ResponseEntity<Inventario> getInventarioPorId(@PathVariable Long id) {
        Inventario buscado = inventarioService.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el inventario con id " + id));
        return new ResponseEntity<>(buscado, HttpStatus.OK);
    }

    @Operation(summary = "Verificar disponibilidad", description = "Consulta la cantidad disponible de un producto en una sucursal específica.")
    @GetMapping("/disponibilidad")
    public ResponseEntity<Integer> verificarDisponibilidad(
            @RequestParam Long idProducto,
            @RequestParam Long idSucursal) {
        int disponible = inventarioService.verificarDisponibilidad(idProducto, idSucursal);
        return new ResponseEntity<>(disponible, HttpStatus.OK);
    }

    @Operation(summary = "Consultar stock", description = "Devuelve la información de stock de un producto en una sucursal específica.")
    @GetMapping("/consulta")
    public ResponseEntity<StockConsultaDTO> consultarStock(
            @RequestParam Long idProducto,
            @RequestParam Long idSucursal) {
        StockConsultaDTO resultado = inventarioService.consultarStock(idProducto, idSucursal);
        return new ResponseEntity<>(resultado, HttpStatus.OK);
    }

    @Operation(summary = "Consultar stock por SKU o nombre", description = "Devuelve la información de stock de un producto en una sucursal específica, filtrando por SKU o nombre.")
    @GetMapping("/consulta-busqueda")
    public ResponseEntity<List<StockConsultaDTO>> consultarStockPorSkuONombre(
            @RequestParam(required = false) String sku,
            @RequestParam(required = false) String nombre,
            @RequestParam Long idSucursal) {
        List<StockConsultaDTO> resultado = inventarioService.consultarStockPorSkuONombre(sku, nombre, idSucursal);
        return new ResponseEntity<>(resultado, HttpStatus.OK);
    }

    @Operation(summary = "Actualizar inventario", description = "Modifica un inventario existente por id.")
    @PutMapping("/{id}")
    public ResponseEntity<Inventario> putInventario(@PathVariable Long id, @Valid @RequestBody Inventario inventario) {
        inventarioService.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el inventario con id " + id));
        inventario.setIdInventario(id);
        Inventario actualizado = inventarioService.guardarInventario(inventario);
        return new ResponseEntity<>(actualizado, HttpStatus.OK);
    }

    @Operation(summary = "Apartar stock (reservar)", description = "Reserva unidades del disponible. Usado por el flujo de pedidos web. 409 si no hay disponible.")
    @PutMapping("/apartar")
    public ResponseEntity<Inventario> apartarStock(@Valid @RequestBody AjusteStockDTO peticion) {
        Inventario actualizado = inventarioService.apartarStock(
                peticion.getIdProducto(), peticion.getIdSucursal(), peticion.getCantidad());
        return new ResponseEntity<>(actualizado, HttpStatus.OK);
    }

    @Operation(summary = "Cancelar reserva", description = "Libera unidades reservadas devolviéndolas al disponible. 409 si excede lo reservado.")
    @PutMapping("/cancelar-reserva")
    public ResponseEntity<Inventario> cancelarReserva(@Valid @RequestBody AjusteStockDTO peticion) {
        Inventario actualizado = inventarioService.cancelarReserva(
                peticion.getIdProducto(), peticion.getIdSucursal(), peticion.getCantidad());
        return new ResponseEntity<>(actualizado, HttpStatus.OK);
    }

    @Operation(summary = "Confirmar reserva", description = "Convierte una reserva en salida física: baja cantidad y reservada. 409 si excede lo reservado.")
    @PutMapping("/confirmar-reserva")
    public ResponseEntity<Inventario> confirmarReserva(@Valid @RequestBody AjusteStockDTO peticion) {
        Inventario actualizado = inventarioService.confirmarReserva(
                peticion.getIdProducto(), peticion.getIdSucursal(), peticion.getCantidad());
        return new ResponseEntity<>(actualizado, HttpStatus.OK);
    }

    @Operation(summary = "Ajustar stock", description = "HU-13: suma o resta stock físico (delta). Usado por recepción de órdenes y ventas. 409 si quedaría bajo lo reservado.")
    @PutMapping("/ajustar")
    public ResponseEntity<Inventario> ajustarStock(@Valid @RequestBody AjusteStockDTO ajuste) {
        Inventario actualizado = inventarioService.ajustarStock(
                ajuste.getIdProducto(), ajuste.getIdSucursal(), ajuste.getCantidad(), null);
        return new ResponseEntity<>(actualizado, HttpStatus.OK);
    }

    @Operation(summary = "Eliminar inventario", description = "Elimina un registro de inventario. 404 si no existe.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarInventario(@PathVariable Long id) {
        inventarioService.eliminarInventario(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}