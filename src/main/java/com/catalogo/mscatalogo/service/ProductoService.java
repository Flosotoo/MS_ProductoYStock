package com.catalogo.mscatalogo.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import com.catalogo.mscatalogo.exception.RecursoDuplicadoException;
import com.catalogo.mscatalogo.model.Categoria;
import com.catalogo.mscatalogo.model.EstadoProducto;
import com.catalogo.mscatalogo.model.Producto;
import com.catalogo.mscatalogo.model.ProductoErrorDTO;
import com.catalogo.mscatalogo.model.ProductoLoteResponse;
import com.catalogo.mscatalogo.repository.ProductoRepository;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

@Service
public class ProductoService {
    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private Validator validator;

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

    public Producto actualizarProducto(Producto producto) {
        if (producto.getEstado() == null) {
            producto.setEstado(EstadoProducto.ACTIVO);
        }
        return productoRepository.save(producto);
    }

    public ProductoLoteResponse guardarProductosParcial(List<Producto> productos) {
        List<Producto> exitosos = new ArrayList<>();
        List<ProductoErrorDTO> errores = new ArrayList<>();

        for (int i = 0; i < productos.size(); i++) {
            Producto producto = productos.get(i);

            // 1. Validación manual de campos (@NotBlank, @NotNull, etc.) por item
            Set<ConstraintViolation<Producto>> violaciones = validator.validate(producto);
            if (!violaciones.isEmpty()) {
                String mensaje = violaciones.stream()
                        .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                        .collect(Collectors.joining(", "));
                errores.add(new ProductoErrorDTO(i, producto.getSku(), producto.getNombre(), mensaje));
                continue;
            }

            // 2. Guardado real, capturando reglas de negocio y errores de BD
            try {
                Producto guardado = guardarProducto(producto);
                exitosos.add(guardado);
            } catch (RecursoDuplicadoException e) {
                errores.add(new ProductoErrorDTO(i, producto.getSku(), producto.getNombre(), e.getMessage()));
            } catch (DataIntegrityViolationException e) {
                errores.add(new ProductoErrorDTO(i, producto.getSku(), producto.getNombre(),
                        "El producto ya existe o viola una restricción de la base de datos"));
            } catch (Exception e) {
                errores.add(new ProductoErrorDTO(i, producto.getSku(), producto.getNombre(),
                        "Error inesperado: " + e.getMessage()));
            }
        }

        return new ProductoLoteResponse(exitosos, errores);
    }

}
