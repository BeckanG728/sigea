package com.institucion.sigea.matricula.entity;

import com.institucion.sigea.core.persistence.AuditableEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

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
public class Matricula extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cod_matricula")
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String codigo;

    @Column(name = "cod_alumno", nullable = false)
    private Integer codAlumno;

    @Column(name = "cod_aula", nullable = false)
    private Integer codAula;

    @Column(name = "cod_anio_academico", nullable = false)
    private Integer codAnioAcademico;

    @Column(name = "fecha_matricula", nullable = false)
    private LocalDateTime fechaMatricula;

    @Column(name = "estado_matricula", nullable = false, length = 20)
    private String estadoMatricula = "ACTIVO";

    @Column(name = "cod_usuario")
    private Integer codUsuario;
}
