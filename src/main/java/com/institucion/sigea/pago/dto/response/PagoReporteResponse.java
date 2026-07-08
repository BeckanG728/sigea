package com.institucion.sigea.pago.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record PagoReporteResponse(
        BigDecimal totalRecaudado,
        int cantidadPagos,
        List<PagoDetalleResponse> detalle
) {}
