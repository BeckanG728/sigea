package com.institucion.sigea.auditoria;

import com.institucion.sigea.core.enums.TipoOperacionAuditoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;

public interface AuditoriaRepository extends JpaRepository<AuditoriaEntity, Long> {

    @Query("""
            SELECT COUNT(a) FROM AuditoriaEntity a
            WHERE a.operacion = :operacion
              AND a.fechaHora >= :desde
              AND (:usuarioId IS NULL OR a.usuario.id = :usuarioId)
            """)
    int countByOperacionAndFechaHoraAfter(
            @Param("operacion") TipoOperacionAuditoria operacion,
            @Param("desde") Instant desde,
            @Param("usuarioId") Long usuarioId);
}
