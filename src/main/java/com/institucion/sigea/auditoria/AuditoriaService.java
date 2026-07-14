package com.institucion.sigea.auditoria;

import com.institucion.sigea.core.enums.TipoOperacionAuditoria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;

public interface AuditoriaService {

    void registrar(AuditoriaEntity auditoria);

    void registrarLogin(Long idUsuario, String ip, String equipo, String navegador);

    void registrarIntentoFallido(Long idUsuario, String ip, String equipo, String navegador);

    int contarIntentosFallidos(Long idUsuario, int minutos);

    Page<AuditoriaEntity> buscar(Long usuarioId, String modulo, TipoOperacionAuditoria operacion, Instant desde, Instant hasta, Pageable pageable);
}
