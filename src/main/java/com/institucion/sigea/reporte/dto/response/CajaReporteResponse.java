package com.institucion.sigea.reporte.dto.response;

import java.math.BigDecimal;
import java.util.List;

/**
 * Respuesta del "Reporte de Caja": ingresos del período agrupados
 * por mes/tipo de concepto y resumen de totales.
 * totalEgresos queda en 0 hasta que se implemente el módulo de egresos.
 */
public record CajaReporteResponse(
        BigDecimal totalIngresos,
        BigDecimal totalEgresos,
        BigDecimal saldoNeto,
        List<CajaFilaResponse> ingresos
) {}
