package com.institucion.sigea.reporte.service;

import com.institucion.sigea.reporte.dto.response.CajaReporteResponse;

import java.time.LocalDateTime;

public interface CajaReporteService {

    /**
     * Ingresos del período agrupados por mes y tipo de concepto,
     * con totales de caja (egresos = 0 por ahora).
     */
    CajaReporteResponse reportar(LocalDateTime desde, LocalDateTime hasta);
}
