package com.catalogo.mscatalogo.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.catalogo.mscatalogo.model.Producto;
import com.catalogo.mscatalogo.repository.ProductoRepository;

@Service
public class ProductoService {

    @Autowired
    private ProductoRepository productoRepository;

    public List<Producto> listarProductos() {
        return productoRepository.findAll();
    }

    public Producto crearProducto(Producto producto) {
        return productoRepository.save(producto);
    }

    public Producto actualizarProducto(Long idProducto, Producto producto) {
        Producto existente = productoRepository.findById(idProducto).orElse(null);
        if (existente != null) {
            existente.setNombre(producto.getNombre());
            existente.setDescripcion(producto.getDescripcion());
            existente.setPrecio(producto.getPrecio());
            return productoRepository.save(existente);
        }
        return null;

    }

    public void eliminarProducto(Long idProducto) {
        productoRepository.deleteById(idProducto);
    }

    public Optional<Producto> getProductoPorMarca(String marca) {
        return productoRepository.findByMarca(marca);
    }

    public Optional<Producto> getProducto(Long idProducto) {
    return productoRepository.findById(idProducto);
}

}
