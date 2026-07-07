package com.institucion.sigea.aula.entity;

import com.institucion.sigea.core.persistence.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "aula", uniqueConstraints = @UniqueConstraint(
        columnNames = {"anio_academico_id", "nivel_id", "grado_id", "seccion"}))
@Getter @Setter
@NoArgsConstructor
public class Aula extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "anio_academico_id", nullable = false)
    private AnioAcademico anioAcademico;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nivel_id", nullable = false)
    private Nivel nivel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "grado_id", nullable = false)
    private Grado grado;

    @Column(nullable = false, length = 2)
    private String seccion; // "A", "B", "C"...

    @Column(nullable = false)
    private short capacidadMaxima;
}