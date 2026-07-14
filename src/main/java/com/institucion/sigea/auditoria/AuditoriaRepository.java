package com.institucion.sigea.auditoria;

import com.institucion.sigea.core.enums.TipoOperacionAuditoria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

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

    @Query("""
    SELECT a FROM AuditoriaEntity a
    WHERE (:usuarioId IS NULL OR a.usuario.id = :usuarioId)
      AND (:modulo IS NULL OR a.modulo = :modulo)
      AND (:operacion IS NULL OR a.operacion = :operacion)
      AND (:desde IS NULL OR a.fechaHora >= :desde)
      AND (:hasta IS NULL OR a.fechaHora <= :hasta)
    """)
    Page<AuditoriaEntity> buscar(
            @Param("usuarioId") Long usuarioId,
            @Param("modulo") String modulo,
            @Param("operacion") TipoOperacionAuditoria operacion,
            @Param("desde") Instant desde,
            @Param("hasta") Instant hasta,
            Pageable pageable);
}
