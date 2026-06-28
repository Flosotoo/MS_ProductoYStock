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
import org.springframework.web.bind.annotation.RestController;

import com.catalogo.mscatalogo.dto.AjusteStockDTO;
import com.catalogo.mscatalogo.dto.InventarioLoteRequest;
import com.catalogo.mscatalogo.dto.InventarioLoteResponse;
import com.catalogo.mscatalogo.exception.RecursoNoEncontradoException;
import com.catalogo.mscatalogo.model.Inventario;
import com.catalogo.mscatalogo.service.InventarioService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/inventario")
public class InventarioController {
    @Autowired
    private InventarioService inventarioService;

    @GetMapping
    public ResponseEntity<List<Inventario>> getInventario() {
        List<Inventario> inventarios = inventarioService.listarInventario();
        if (inventarios.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(inventarios, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<Inventario> postInventario(@Valid @RequestBody Inventario inventario) {
        Inventario nuevo = inventarioService.guardarInventario(inventario);
        return new ResponseEntity<>(nuevo, HttpStatus.CREATED);
    }

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

    @GetMapping("/{id}")
    public ResponseEntity<Inventario> getInventarioPorId(@PathVariable Long id) {
        Inventario buscado = inventarioService.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el inventario con id " + id));
        return new ResponseEntity<>(buscado, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Inventario> putInventario(@PathVariable Long id, @Valid @RequestBody Inventario inventario) {
        inventarioService.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el inventario con id " + id));

        inventario.setIdInventario(id);
        Inventario actualizado = inventarioService.guardarInventario(inventario);
        return new ResponseEntity<>(actualizado, HttpStatus.OK);
    }

    @PutMapping("/apartar")
    public ResponseEntity<Inventario> apartarStock(@Valid @RequestBody AjusteStockDTO peticion) {
        Inventario actualizado = inventarioService.apartarStock(
                peticion.getIdProducto(), peticion.getIdSucursal(), peticion.getCantidad());
        return new ResponseEntity<>(actualizado, HttpStatus.OK);
    }

    @PutMapping("/cancelar-reserva")
    public ResponseEntity<Inventario> cancelarReserva(@Valid @RequestBody AjusteStockDTO peticion) {
        Inventario actualizado = inventarioService.cancelarReserva(
                peticion.getIdProducto(), peticion.getIdSucursal(), peticion.getCantidad());
        return new ResponseEntity<>(actualizado, HttpStatus.OK);
    }

    @PutMapping("/ajustar")
    public ResponseEntity<Inventario> ajustarStock(@Valid @RequestBody AjusteStockDTO ajuste) {
        Inventario actualizado = inventarioService.ajustarStock(
                ajuste.getIdProducto(), ajuste.getIdSucursal(), ajuste.getCantidad(), null);
        return new ResponseEntity<>(actualizado, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarInventario(@PathVariable Long id) {
        inventarioService.eliminarInventario(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
