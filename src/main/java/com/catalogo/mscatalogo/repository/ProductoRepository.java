package com.catalogo.mscatalogo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.catalogo.mscatalogo.model.Producto;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {
    boolean existsBySku(String sku);

}
