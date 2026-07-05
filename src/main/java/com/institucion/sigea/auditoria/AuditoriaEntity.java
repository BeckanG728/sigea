package com.institucion.sigea.auditoria;

import com.institucion.sigea.core.enums.TipoOperacionAuditoria;
import com.institucion.sigea.usuario.entity.Usuario;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "auditoria")
@Getter
@Setter
public class AuditoriaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cod_auditoria")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cod_usuario")
    private Usuario usuario;

    @Column(nullable = false, length = 50)
    private String modulo;

    @Column(name = "tabla_afectada", length = 50)
    private String tablaAfectada;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoOperacionAuditoria operacion;

    @Column(name = "codigo_registro")
    private Long codigoRegistro;

    @Lob
    @Column(name = "valor_anterior")
    private String valorAnterior;

    @Lob
    @Column(name = "valor_nuevo")
    private String valorNuevo;

    @Column(nullable = false)
    private Instant fechaHora;

    @Column(nullable = false, length = 45)
    private String ipOrigen;

    @Column(length = 100)
    private String equipo;

    @Column(length = 150)
    private String navegador;
}
