package com.institucion.sigea.aula.entity;

import com.institucion.sigea.core.persistence.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "grado", uniqueConstraints = @UniqueConstraint(columnNames = {"nivel_id", "nombre_grado"}))
@Getter @Setter
@NoArgsConstructor
public class Grado extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nivel_id", nullable = false)
    private Nivel nivel;

    @Column(nullable = false, length = 20)
    private String nombreGrado; // ej. "1°"
}