package com.institucion.sigea.reporte.dto.response;

import com.institucion.sigea.core.enums.TipoOperacionAuditoria;

import java.time.Instant;

public record AuditoriaReporteResponse(
        Long id,
        Long codUsuario,
        String nombreUsuario,
        String modulo,
        String tablaAfectada,
        TipoOperacionAuditoria operacion,
        String codigoRegistro,
        Instant fechaHora,
        String ipOrigen
) {}
