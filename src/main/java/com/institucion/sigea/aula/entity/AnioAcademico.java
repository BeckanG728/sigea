package com.institucion.sigea.aula.entity;

import com.institucion.sigea.core.persistence.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "anio_academico")
@Getter @Setter
@NoArgsConstructor
public class AnioAcademico extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cod_anio_academico")
    private Long id;

    @Column(nullable = false, unique = true)
    private Integer anio;
}