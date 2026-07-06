package com.institucion.sigea.alumno.entity;

import com.institucion.sigea.core.persistence.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tipo_documento")
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TipoDocumento extends BaseEntity {

    @Column(nullable = false, unique = true, length = 40)
    private String descripcion; // DNI, RUC, CE, Pasaporte
}