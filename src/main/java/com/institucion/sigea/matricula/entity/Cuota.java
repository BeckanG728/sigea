package com.institucion.sigea.matricula.entity;

import com.institucion.sigea.core.persistence.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "cuota")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Cuota extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cod_cuota")
    private Integer codCuota;

    @Column(name = "cod_matricula", nullable = false)
    private Integer codMatricula;

    @Column(name = "cod_concepto", nullable = false)
    private Integer codConcepto;

    @Column(name = "monto_pagar", nullable = false, precision = 10, scale = 2)
    private BigDecimal montoPagar;

    @Column(name = "orden_pago", nullable = false)
    private Short ordenPago;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_cuota", nullable = false, length = 20)
    private EstadoCuota estadoCuota;

    @Column(name = "numero_recibo", length = 20)
    private String numeroRecibo;

    @Column(name = "fecha_pago")
    private LocalDateTime fechaPago;

    @Column(name = "fecha_registro", nullable = false, updatable = false)
    private LocalDateTime fechaRegistro;

    @PrePersist
    void prePersist() {
        if (fechaRegistro == null) {
            fechaRegistro = LocalDateTime.now();
        }
        if (estadoCuota == null) {
            estadoCuota = EstadoCuota.PENDIENTE;
        }
    }
}