package com.institucion.sigea.reporte.dto.response;

import com.institucion.sigea.core.enums.TipoOperacionAuditoria;

import java.time.Instant;

public record AuditoriaReporteResponse(
        Long id,
        Long codUsuario,
        String modulo,
        TipoOperacionAuditoria operacion,
        Long codigoRegistro,
        Instant fechaHora,
        String ipOrigen
) {}
