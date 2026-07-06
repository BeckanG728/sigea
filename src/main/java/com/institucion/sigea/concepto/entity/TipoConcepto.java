package com.institucion.sigea.concepto.entity;

import com.institucion.sigea.core.persistence.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tipo_concepto")
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TipoConcepto extends BaseEntity {

    @Column(nullable = false, unique = true, length = 60)
    private String nombre; // ej. "Matrícula", "Pensión"
}