package com.institucion.sigea.reporte.dto.response;

import java.math.BigDecimal;

/**
 * Ingresos agrupados por mes y tipo de concepto para el "Reporte de Caja".
 */
public record CajaFilaResponse(
        int anio,
        int mes,               // 1..12
        String nombreMes,      // "Enero", "Febrero", ...
        String concepto,       // nombre del tipo de concepto: "Matrícula", "Pensión", ...
        long cantidadPagos,
        BigDecimal total
) {}
