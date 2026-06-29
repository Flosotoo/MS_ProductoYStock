package com.catalogo.mscatalogo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.catalogo.mscatalogo.model.Categoria;
import com.catalogo.mscatalogo.model.Producto;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {
    boolean existsBySku(String sku);

    List<Producto> findByNombreContainingIgnoreCase(String nombre);
    List<Producto> findByCategoria(Categoria categoria);
    Optional<Producto> findBySku(String sku);

}
