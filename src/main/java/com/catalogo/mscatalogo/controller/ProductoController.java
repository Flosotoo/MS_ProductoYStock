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

import com.catalogo.mscatalogo.dto.ProductoLoteRequest;
import com.catalogo.mscatalogo.dto.ProductoLoteResponse;
import com.catalogo.mscatalogo.exception.RecursoNoEncontradoException;
import com.catalogo.mscatalogo.model.Categoria;
import com.catalogo.mscatalogo.model.Producto;
import com.catalogo.mscatalogo.service.ProductoService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {
    @Autowired
    private ProductoService productoService;

    @GetMapping
    public ResponseEntity<List<Producto>> getProductos() {
        List<Producto> productos = productoService.listarProductos();
        if (productos.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(productos, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<Producto> postProducto(@Valid @RequestBody Producto producto) {
        Producto nuevo = productoService.guardarProducto(producto);
        return new ResponseEntity<>(nuevo, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Producto> actualizarProducto(@PathVariable Long id, @Valid @RequestBody Producto producto) {
        Producto existente = productoService.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el producto con id " + id));
        producto.setIdProducto(existente.getIdProducto());
        producto.setSku(existente.getSku());
        Producto actualizado = productoService.actualizarProducto(producto);                                                                         
        return new ResponseEntity<>(actualizado, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Producto> getProducto(@PathVariable Long id) {
        Producto buscado = productoService.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el producto con id " + id));
        return new ResponseEntity<>(buscado, HttpStatus.OK);
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<Producto>> buscarPorNombre(@RequestParam String nombre) {
        List<Producto> resultado = productoService.buscarPorNombre(nombre);
        if (resultado.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(resultado, HttpStatus.OK);
    }

    @GetMapping("/categoria/{categoria}")
    public ResponseEntity<List<Producto>> buscarPorCategoria(@PathVariable Categoria categoria) {
        List<Producto> resultado = productoService.buscarPorCategoria(categoria);
        if (resultado.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(resultado, HttpStatus.OK);
    }

    @PostMapping("/lote-parcial")
    public ResponseEntity<ProductoLoteResponse> postProductosLoteParcial(
            @Valid @RequestBody ProductoLoteRequest request) {
        ProductoLoteResponse resultado = productoService.guardarProductosParcial(request.getProductos());

        if (resultado.getErrores().isEmpty()) {
            return new ResponseEntity<>(resultado, HttpStatus.CREATED); 
        } else if (resultado.getExitosos().isEmpty()) {
            return new ResponseEntity<>(resultado, HttpStatus.BAD_REQUEST); 
        } else {
            return new ResponseEntity<>(resultado, HttpStatus.MULTI_STATUS); 
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarProducto(@PathVariable Long id) {
        productoService.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el producto con id " + id));
        productoService.eliminarProducto(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
