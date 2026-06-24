package com.catalogo.mscatalogo.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "operacion_stock", uniqueConstraints = {
        @UniqueConstraint(name = "uk_operacion", columnNames = { "id_operacion" }) })
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OperacionStock {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "id_operacion", nullable = false, length = 50)
    private String idOperacion;

}
