package com.institucion.sigea.reporte.service;

import com.institucion.sigea.matricula.entity.EstadoCuota;
import com.institucion.sigea.reporte.dto.response.DeudaDetalleReporteResponse;

import java.util.List;

public interface DeudaDetalleReporteService {

    /**
     * Lista las cuotas adeudadas con datos del alumno, documento, concepto,
     * vencimiento y días de atraso.
     *
     * @param anio    año académico (opcional; null = todos)
     * @param estados estados a incluir (opcional; null/vacío = PENDIENTE y BLOQUEADA)
     */
    List<DeudaDetalleReporteResponse> reportar(Integer anio, List<EstadoCuota> estados);
}
