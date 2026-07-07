package com.institucion.sigea.aula.entity;

import com.institucion.sigea.core.persistence.AuditableEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "grado", uniqueConstraints = @UniqueConstraint(columnNames = {"nivel_id", "nombre_grado"}))
@Getter @Setter
@NoArgsConstructor
public class Grado extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cod_grado")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nivel_id", nullable = false)
    private Nivel nivel;

    @Column(nullable = false, length = 20)
    private String nombreGrado; // ej. "1°"
}