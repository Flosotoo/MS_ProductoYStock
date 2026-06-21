package com.catalogo.mscatalogo.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import com.catalogo.mscatalogo.exception.RecursoDuplicadoException;
import com.catalogo.mscatalogo.exception.RecursoNoEncontradoException;
import com.catalogo.mscatalogo.model.EstadoProducto;
import com.catalogo.mscatalogo.model.EstadoStock;
import com.catalogo.mscatalogo.model.Inventario;
import com.catalogo.mscatalogo.model.InventarioErrorDTO;
import com.catalogo.mscatalogo.model.InventarioLoteResponse;
import com.catalogo.mscatalogo.model.Producto;
import com.catalogo.mscatalogo.repository.InventarioRepository;
import com.catalogo.mscatalogo.repository.ProductoRepository;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.util.Set;

@Service
public class InventarioService {
    @Autowired
    private InventarioRepository inventarioRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private Validator validator;

    public Inventario guardarInventario(Inventario inventario) {
        Producto productoCompleto = productoRepository
                .findById(inventario.getProducto().getIdProducto())
                .orElse(null);

        if (productoCompleto == null) {
            throw new RuntimeException("El producto indicado no existe");
        }

        inventario.setProducto(productoCompleto);
        inventario.setEstadoStock(calcularEstadoStock(inventario.getCantidad(), inventario.getUmbralMinimo(),
                productoCompleto.getEstado()));
        return inventarioRepository.save(inventario);
    }

    public InventarioLoteResponse guardarInventariosParcial(List<Inventario> inventarios) {
        List<Inventario> exitosos = new ArrayList<>();
        List<InventarioErrorDTO> errores = new ArrayList<>();

        for (int i = 0; i < inventarios.size(); i++) {
            Inventario inventario = inventarios.get(i);
            String idProductoRef = (inventario.getProducto() != null
                    && inventario.getProducto().getIdProducto() != null)
                            ? inventario.getProducto().getIdProducto().toString()
                            : "desconocido";

            // 1. Validación manual de campos (@NotNull, @Min, etc.) por item
            Set<ConstraintViolation<Inventario>> violaciones = validator.validate(inventario);
            if (!violaciones.isEmpty()) {
                String mensaje = violaciones.stream()
                        .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                        .collect(Collectors.joining(", "));
                errores.add(new InventarioErrorDTO(i, idProductoRef, inventario.getIdSucursal(), mensaje));
                continue;
            }

            // 2. Guardado real, capturando reglas de negocio y errores de BD
            try {
                Inventario guardado = guardarInventario(inventario);
                exitosos.add(guardado);
            } catch (RecursoNoEncontradoException | RecursoDuplicadoException e) {
                errores.add(new InventarioErrorDTO(i, idProductoRef, inventario.getIdSucursal(), e.getMessage()));
            } catch (DataIntegrityViolationException e) {
                errores.add(new InventarioErrorDTO(i, idProductoRef, inventario.getIdSucursal(),
                        "El inventario ya existe o viola una restricción de la base de datos"));
            } catch (Exception e) {
                errores.add(new InventarioErrorDTO(i, idProductoRef, inventario.getIdSucursal(),
                        "Error inesperado: " + e.getMessage()));
            }
        }

        return new InventarioLoteResponse(exitosos, errores);
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

    public void eliminarInventario(Long id) {
        Inventario existente = inventarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el inventario con id " + id));
        inventarioRepository.delete(existente);
    }

}
