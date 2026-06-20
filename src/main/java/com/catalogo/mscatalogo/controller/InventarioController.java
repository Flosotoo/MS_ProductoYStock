package com.catalogo.mscatalogo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.catalogo.mscatalogo.model.Inventario;
import com.catalogo.mscatalogo.service.InventarioService;

@RestController
@RequestMapping("/api/inventario")
public class InventarioController {
    @Autowired
    private InventarioService inventarioService;

    @GetMapping
    public ResponseEntity<List<Inventario>> getInventario() {
        List<Inventario> inventarios = inventarioService.listarInventario();
        if (inventarios.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(inventarios, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<Inventario> postInventario(@RequestBody Inventario inventario) {
        try {
            Inventario nuevo = inventarioService.guardarInventario(inventario);
            return new ResponseEntity<>(nuevo, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.CONFLICT); // 409 si ya existe inventario para ese producto+sucursal
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Inventario> getInventarioPorId(@PathVariable Long id) {
        Inventario buscado = inventarioService.findById(id).orElse(null);
        if (buscado == null) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(buscado, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Inventario> putInventario(@PathVariable Long id, @RequestBody Inventario inventario) {
        inventario.setIdInventario(id);
        Inventario actualizado = inventarioService.guardarInventario(inventario);
        return new ResponseEntity<>(actualizado, HttpStatus.OK);
    }
}
