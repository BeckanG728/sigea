package com.institucion.sigea.alumno.entity;

import com.institucion.sigea.core.crypto.AesConverter;
import com.institucion.sigea.core.persistence.AuditableEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "alumno")
@Getter @Setter
@NoArgsConstructor
public class Alumno extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cod_alumno")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tipo_documento_id", nullable = false)
    private TipoDocumento tipoDocumento;

    @Column(nullable = false, length = 255)
    @Convert(converter = AesConverter.class)
    private String numeroDocumento; // se guarda cifrado

    @Column(nullable = false, length = 80)
    private String nombres;

    @Column(nullable = false, length = 60)
    private String apellidoPaterno;

    @Column(nullable = false, length = 60)
    private String apellidoMaterno;

    @Column(nullable = false, length = 255)
    @Convert(converter = AesConverter.class)
    private String fechaNacimiento; // texto ISO (yyyy-MM-dd), se guarda cifrado
}