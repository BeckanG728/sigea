package com.institucion.sigea.matricula.entity;

import com.institucion.sigea.core.persistence.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Matrícula de un alumno en un aula, para un año académico determinado.
 * Un alumno no puede matricularse dos veces en el mismo año (unique key).
 */
@Entity
@Table(
        name = "matricula",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_matricula_alumno_anio",
                columnNames = {"cod_alumno", "cod_anio_academico"}
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Matricula extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cod_matricula")
    private Integer codMatricula;

    /** FK lógica a alumno.entity.Alumno — no se referencia por JPA para no acoplar módulos. */
    @Column(name = "cod_alumno", nullable = false)
    private Integer codAlumno;

    /** FK lógica a aula.entity.Aula. */
    @Column(name = "cod_aula", nullable = false)
    private Integer codAula;

    /** FK lógica a aula.entity.AnioAcademico. */
    @Column(name = "cod_anio_academico", nullable = false)
    private Integer codAnioAcademico;

    @Column(name = "fecha_matricula", nullable = false)
    private LocalDateTime fechaMatricula;
}
