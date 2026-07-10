package com.institucion.sigea.usuario.entity;

import com.institucion.sigea.core.crypto.AesConverter;
import com.institucion.sigea.core.crypto.AesDeterministicConverter;
import com.institucion.sigea.core.persistence.AuditableEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "usuario")
@Getter
@Setter
@NoArgsConstructor
public class Usuario extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cod_usuario")
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String codigo;

    @Column(name = "nombre_usuario", nullable = false, unique = true, length = 255)
    @Convert(converter = AesDeterministicConverter.class)
    private String nombreUsuario;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(name = "primer_apellido", nullable = false, length = 100)
    private String primerApellido;

    @Column(name = "numero_documento", nullable = false, unique = true, length = 255)
    @Convert(converter = AesDeterministicConverter.class)
    private String numeroDocumento;

    @Column(nullable = false, length = 255)
    private String password;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_rol", nullable = false)
    private Rol rol;

    @Column(nullable = false)
    private boolean dosFactorHabilitado;

    @Column(length = 255)
    @Convert(converter = AesConverter.class)
    private String totpSecret;
}
