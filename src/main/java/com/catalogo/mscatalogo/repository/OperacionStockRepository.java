package com.catalogo.mscatalogo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.catalogo.mscatalogo.model.OperacionStock;

public interface OperacionStockRepository extends JpaRepository<OperacionStock, Long>{
    boolean existsByIdOperacion(String idOperacion);
}
