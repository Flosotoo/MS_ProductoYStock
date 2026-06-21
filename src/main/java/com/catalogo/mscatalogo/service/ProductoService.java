package com.catalogo.mscatalogo.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
        }
        if (producto.getEstado() == null) {
            producto.setEstado(EstadoProducto.ACTIVO);
        }
        return productoRepository.save(producto); // save() inserta o actualiza según si el id viene null o no
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
        String prefijo = categoria.name().substring(0, 3); // PER, COL, etc.
        String sufijo = String.valueOf(System.currentTimeMillis() % 100000); // últimos 5 dígitos
        return prefijo + sufijo;
    }

}
