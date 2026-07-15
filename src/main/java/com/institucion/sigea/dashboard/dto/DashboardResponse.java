package com.institucion.sigea.dashboard.dto;

import com.institucion.sigea.core.api.PageResponse;

public record DashboardResponse(
        long totalMatriculas,
        long totalAulasActivas,
        long pagosPendientes,
        PageResponse<MatriculaDashboardResponse> matriculasRecientes
) {}
