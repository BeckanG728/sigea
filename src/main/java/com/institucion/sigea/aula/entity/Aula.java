package com.institucion.sigea.aula.entity;

import com.institucion.sigea.core.persistence.AuditableEntity;
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
@SequenceGenerator(name = "seq_aula", sequenceName = "seq_aula", allocationSize = 1)
public class Aula extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_aula")
    @Column(name = "cod_aula")
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String codigo;

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