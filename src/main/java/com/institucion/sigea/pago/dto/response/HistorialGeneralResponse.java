package com.institucion.sigea.pago.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record HistorialGeneralResponse(
        List<DeudaHistorialResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        long cantidadAlumnosDeudores,
        BigDecimal totalDeuda
) {}
