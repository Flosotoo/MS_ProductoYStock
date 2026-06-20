package com.catalogo.mscatalogo.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.catalogo.mscatalogo.model.EstadoProducto;
import com.catalogo.mscatalogo.model.EstadoStock;
import com.catalogo.mscatalogo.model.Inventario;
import com.catalogo.mscatalogo.model.Producto;
import com.catalogo.mscatalogo.repository.InventarioRepository;
import com.catalogo.mscatalogo.repository.ProductoRepository;

@Service
public class InventarioService {
    @Autowired
    private InventarioRepository inventarioRepository;

    @Autowired
    private ProductoRepository productoRepository;

    public Inventario guardarInventario(Inventario inventario) {
        Producto productoCompleto = productoRepository
                .findById(inventario.getProducto().getIdProducto())
                .orElse(null);

        if (productoCompleto == null) {
            throw new RuntimeException("El producto indicado no existe");
        }

        inventario.setProducto(productoCompleto);
        inventario.setEstadoStock(calcularEstadoStock(inventario.getCantidad(), inventario.getUmbralMinimo(), productoCompleto.getEstado()));
        return inventarioRepository.save(inventario);
    }
 
    public List<Inventario> listarInventario() {
        return inventarioRepository.findAll();
    }
 
    public Optional<Inventario> findById(Long id) {
        return inventarioRepository.findById(id);
    }
 
    private EstadoStock calcularEstadoStock(int cantidad, int umbralMinimo, EstadoProducto estadoProducto) {
        EstadoStock calculado;

        if (cantidad <= 0) {
            calculado = EstadoStock.CRITICO;
        } else if (cantidad <= umbralMinimo) {
            calculado = EstadoStock.BAJO;
        } else {
            calculado = EstadoStock.NORMAL;
        }
        if (estadoProducto == EstadoProducto.DESCONTINUADO && calculado == EstadoStock.CRITICO) {
            calculado = EstadoStock.BAJO;
        }

        return calculado;
    }
}
