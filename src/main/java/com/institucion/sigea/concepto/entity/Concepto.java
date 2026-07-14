package com.institucion.sigea.concepto.entity;

import com.institucion.sigea.aula.entity.AnioAcademico;
import com.institucion.sigea.core.persistence.AuditableEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "concepto", uniqueConstraints = @UniqueConstraint(
        columnNames = {"anio_academico_id", "nombre_concepto"}))
@Getter @Setter
@NoArgsConstructor
public class Concepto extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cod_concepto")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "anio_academico_id", nullable = false)
    private AnioAcademico anioAcademico;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tipo_concepto_id", nullable = false)
    private TipoConcepto tipoConcepto;

    @Column(nullable = false, length = 80)
    private String nombreConcepto;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal monto;

    @Column(nullable = false)
    private short ordenPago;

    @Column(nullable = false)
    private boolean obligatorio;

    @Column(nullable = false, length = 20)
    private String tipo; // FIJO, MENSUAL, OPCIONAL
}