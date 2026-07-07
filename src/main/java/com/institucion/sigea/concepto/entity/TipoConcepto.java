package com.institucion.sigea.concepto.entity;

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
@Table(name = "tipo_concepto")
@Getter @Setter
@NoArgsConstructor
public class TipoConcepto extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cod_tipo_concepto")
    private Long id;

    @Column(nullable = false, unique = true, length = 60)
    private String nombre; // ej. "Matrícula", "Pensión"
}