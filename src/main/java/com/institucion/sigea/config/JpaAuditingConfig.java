package com.institucion.sigea.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Habilita el llenado automático de fechaRegistro/fechaModificacion en
 * BaseEntity vía @CreatedDate/@LastModifiedDate (AuditingEntityListener).
 * Separado de CacheConfig porque son responsabilidades distintas.
 */
@Configuration
@EnableJpaAuditing(auditorAwareRef = "springSecurityAuditorAware")
public class JpaAuditingConfig {
}
