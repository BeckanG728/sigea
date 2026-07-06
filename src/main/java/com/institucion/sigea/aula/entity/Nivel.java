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
@Table(name = "nivel")
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Nivel extends BaseEntity {

    @Column(nullable = false, unique = true, length = 30)
    private String nombre; // ej. "Inicial", "Primaria", "Secundaria"
}