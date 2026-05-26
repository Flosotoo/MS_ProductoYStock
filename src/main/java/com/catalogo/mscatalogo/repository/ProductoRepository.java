package com.catalogo.mscatalogo.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.catalogo.mscatalogo.model.Producto;

public interface ProductoRepository extends JpaRepository<Producto, Long>{
    Optional<Producto> findByMarca(String marca);
}
