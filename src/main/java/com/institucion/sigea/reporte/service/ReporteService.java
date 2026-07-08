package com.institucion.sigea.reporte.service;

import com.institucion.sigea.matricula.dto.response.MatriculaReporteResponse;
import com.institucion.sigea.pago.dto.response.DeudaAlumnoResponse;
import com.institucion.sigea.pago.dto.response.PagoReporteResponse;
import com.institucion.sigea.reporte.dto.response.AuditoriaReporteResponse;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

public interface ReporteService {

    List<MatriculaReporteResponse> reportarMatriculas(
            Integer anioAcademico, Long codNivel, Long codGrado, Integer codAula);

    PagoReporteResponse reportarPagos(LocalDateTime desde, LocalDateTime hasta);

    List<DeudaAlumnoResponse> reportarDeudas();

    List<AuditoriaReporteResponse> reportarAuditoria(
            Long codUsuario, String modulo, Instant desde, Instant hasta);
}
