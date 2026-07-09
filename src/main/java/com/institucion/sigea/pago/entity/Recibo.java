package com.institucion.sigea.pago.entity;

import com.institucion.sigea.core.persistence.AuditableEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "recibo",
        uniqueConstraints = @UniqueConstraint(name = "uk_recibo_numero", columnNames = "numero_recibo")
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SequenceGenerator(name = "seq_recibo", sequenceName = "seq_numero_recibo", allocationSize = 1)
public class Recibo extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_recibo")
    @Column(name = "cod_recibo")
    private Long id;

    @Column(name = "numero_recibo", nullable = false, length = 20)
    private String numeroRecibo;

    @Column(name = "cod_pago", nullable = false, unique = true)
    private Integer codPago;

    @Column(name = "anio", nullable = false)
    private Integer anio;

    @Column(name = "correlativo", nullable = false)
    private Integer correlativo;

    @Column(name = "fecha_emision", nullable = false)
    private LocalDateTime fechaEmision;

    @PrePersist
    void prePersist() {
        if (fechaEmision == null) {
            fechaEmision = LocalDateTime.now();
        }
    }
}
