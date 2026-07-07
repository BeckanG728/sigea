package com.institucion.sigea.parametro.entity;

import com.institucion.sigea.core.persistence.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "parametro")
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Parametro extends BaseEntity {

    @Column(nullable = false, unique = true, length = 60)
    private String clave;

    @Column(nullable = false, length = 255)
    private String valor;

    @Column(length = 200)
    private String descripcion;
}