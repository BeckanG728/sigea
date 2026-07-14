package com.institucion.sigea.core.persistence;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import com.institucion.sigea.usuario.entity.Usuario;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;

import java.time.Instant;

@Getter
@Setter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class AuditableEntity {

    @Version
    private Long version;

    @Column(nullable = false)
    private boolean estado = true;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant fechaRegistro;

    @LastModifiedDate
    @Column(nullable = false)
    private Instant fechaModificacion;

    @CreatedBy
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_creacion_id", updatable = false)
    @JsonIgnore
    private Usuario usuarioCreacion;

    @LastModifiedBy
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_modificacion_id")
    @JsonIgnore
    private Usuario usuarioModificacion;
}
