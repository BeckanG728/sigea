package com.institucion.sigea.core.persistence;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Getter
@Setter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Control de concurrencia optimista (JPA incrementa este valor en cada
     * UPDATE y lanza OptimisticLockException si detecta una edición basada
     * en una versión desactualizada).
     */
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
}

