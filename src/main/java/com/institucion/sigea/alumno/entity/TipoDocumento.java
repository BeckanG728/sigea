package com.institucion.sigea.alumno.entity;

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
@Table(name = "tipo_documento")
@Getter @Setter
@NoArgsConstructor
public class TipoDocumento extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cod_tipo_documento")
    private Long id;

    @Column(nullable = false, unique = true, length = 40)
    private String descripcion; // DNI, RUC, CE, Pasaporte
}