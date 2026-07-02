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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/productos")
@Tag(name = "Productos", description = "Gestión del catálogo de productos (HU-10, HU-11, HU-14, HU-49)")
public class ProductoController {
    @Autowired
    private ProductoService productoService;

    @Operation(summary = "Listar todos los productos", description = "Devuelve el catálogo completo. 204 si está vacío.")
    @GetMapping
    public ResponseEntity<List<Producto>> getProductos() {
        List<Producto> productos = productoService.listarProductos();
        if (productos.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(productos, HttpStatus.OK);
    }

    @Operation(summary = "Crear un nuevo producto", description = "Crea un nuevo producto en el catálogo. SKU se genera automáticamente si no se proporciona. Estado por defecto: ACTIVO.")
    @PostMapping
    public ResponseEntity<Producto> postProducto(@Valid @RequestBody Producto producto) {
        Producto nuevo = productoService.guardarProducto(producto);
        return new ResponseEntity<>(nuevo, HttpStatus.CREATED);
    }

    @Operation(summary = "Actualizar un producto existente", description = "Actualiza los datos de un producto en el catálogo. SKU no se puede cambiar. Devuelve 404 si el producto no existe.")
    @PutMapping("/{id}")
    public ResponseEntity<Producto> actualizarProducto(@PathVariable Long id, @Valid @RequestBody Producto producto) {
        Producto existente = productoService.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el producto con id " + id));
        producto.setIdProducto(existente.getIdProducto());
        producto.setSku(existente.getSku());
        Producto actualizado = productoService.actualizarProducto(producto);                                                                         
        return new ResponseEntity<>(actualizado, HttpStatus.OK);
    }

    @Operation(summary = "Obtener un producto por ID", description = "Devuelve los detalles de un producto específico. Devuelve 404 si el producto no existe.")
    @GetMapping("/{id}")
    public ResponseEntity<Producto> getProducto(@PathVariable Long id) {
        Producto buscado = productoService.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el producto con id " + id));
        return new ResponseEntity<>(buscado, HttpStatus.OK);
    }

    @Operation(summary = "Buscar productos por nombre", description = "Devuelve una lista de productos que coinciden con el nombre proporcionado. Devuelve 204 si no se encuentran resultados.")
    @GetMapping("/buscar")
    public ResponseEntity<List<Producto>> buscarPorNombre(@RequestParam String nombre) {
        List<Producto> resultado = productoService.buscarPorNombre(nombre);
        if (resultado.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(resultado, HttpStatus.OK);
    }

    @Operation(summary = "Buscar por categoría", description = "HU-10: filtra productos por categoría.")
    @GetMapping("/categoria/{categoria}")
    public ResponseEntity<List<Producto>> buscarPorCategoria(@PathVariable Categoria categoria) {
        List<Producto> resultado = productoService.buscarPorCategoria(categoria);
        if (resultado.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(resultado, HttpStatus.OK);
    }

    @Operation(summary = "Crear productos en lote", description = "Carga múltiple. 201 todo OK, 207 parcial, 400 nada se guardó.")
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

    @Operation(summary = "Eliminar producto", description = "HU-14: elimina un producto. 404 si no existe.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarProducto(@PathVariable Long id) {
        productoService.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el producto con id " + id));
        productoService.eliminarProducto(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
