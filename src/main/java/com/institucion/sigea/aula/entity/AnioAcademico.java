package com.institucion.sigea.aula.entity;

import com.institucion.sigea.core.persistence.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "anio_academico")
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AnioAcademico extends BaseEntity {

    @Column(nullable = false, unique = true)
    private Integer anio;
}