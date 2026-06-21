package com.catalogo.mscatalogo.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.catalogo.mscatalogo.exception.RecursoDuplicadoException;
import com.catalogo.mscatalogo.model.Categoria;
import com.catalogo.mscatalogo.model.EstadoProducto;
import com.catalogo.mscatalogo.model.Producto;
import com.catalogo.mscatalogo.repository.ProductoRepository;

@Service
public class ProductoService {
    @Autowired
    private ProductoRepository productoRepository;

    public Producto guardarProducto(Producto producto) {
        if (producto.getSku() == null) {
            producto.setSku(generarSku(producto.getCategoria()));
        } else if (productoRepository.existsBySku(producto.getSku())) {
            throw new RecursoDuplicadoException("Ya existe un producto con el SKU: " + producto.getSku());
        }
        if (producto.getEstado() == null) {
            producto.setEstado(EstadoProducto.ACTIVO);
        }
        return productoRepository.save(producto);
    }

    public void eliminarProducto(Long id) {
        productoRepository.deleteById(id);
    }

    public List<Producto> listarProductos() {
        return productoRepository.findAll();
    }

    public Optional<Producto> findById(Long id) {
        return productoRepository.findById(id);
    }

    private String generarSku(Categoria categoria) {
        String prefijo = categoria.name().substring(0, 3);
        String sku;
        do {
            String sufijo = UUID.randomUUID().toString()
                    .replace("-", "")
                    .substring(0, 5)
                    .toUpperCase();
            sku = prefijo + sufijo;
        } while (productoRepository.existsBySku(sku));
        return sku;
    }

}
