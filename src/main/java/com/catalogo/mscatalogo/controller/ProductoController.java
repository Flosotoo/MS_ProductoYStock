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

import com.catalogo.mscatalogo.model.Producto;
import com.catalogo.mscatalogo.service.ProductoService;

@RestController
@RequestMapping("/api/v1/catalogo")
public class ProductoController {

    @Autowired
    private ProductoService productoService;

    @GetMapping
    public ResponseEntity<List<Producto>> getProductos() {
        List<Producto> lista = productoService.listarProductos();
        if (lista.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(lista, HttpStatus.OK);
    }

    @GetMapping("/{idProducto}")
    public ResponseEntity<Producto> getProducto(@PathVariable Long idProducto) {
        Producto buscado = productoService.getProducto(idProducto).orElse(null);
        if (buscado == null) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(buscado, HttpStatus.OK);
    }

    @GetMapping("marca/{marca}")
    public ResponseEntity<Producto> getMarca(@PathVariable String marca) {
        Producto buscado = productoService.getProductoPorMarca(marca).orElse(null);
        if (buscado == null) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(buscado, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<Producto> postProducto(@RequestBody Producto producto) {
        try {
            Producto nuevo = productoService.crearProducto(producto);
            return new ResponseEntity<>(nuevo, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
    }

    @PutMapping("/{idProducto}")
    public ResponseEntity<Producto> putProducto(
            @PathVariable Long idProducto,
            @RequestBody Producto producto) {
        try {
            Producto actualizado = productoService.actualizarProducto(idProducto, producto);
            return new ResponseEntity<>(actualizado, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{idProducto}")
    public ResponseEntity<HttpStatus> deleteProducto(@PathVariable Long idProducto) {
        try {
            productoService.eliminarProducto(idProducto);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
