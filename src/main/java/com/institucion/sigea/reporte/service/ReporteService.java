package com.institucion.sigea.reporte.service;

import com.institucion.sigea.core.api.PageResponse;
import com.institucion.sigea.core.enums.TipoOperacionAuditoria;
import com.institucion.sigea.matricula.dto.response.MatriculaReporteResponse;
import com.institucion.sigea.pago.dto.response.DeudaAlumnoResponse;
import com.institucion.sigea.pago.dto.response.PagoReporteResponse;
import com.institucion.sigea.reporte.dto.response.AuditoriaReporteResponse;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

public interface ReporteService {

    List<MatriculaReporteResponse> reportarMatriculas(
            Integer anioAcademico, Long codNivel, Long codGrado, Integer codAula);

    PagoReporteResponse reportarPagos(LocalDateTime desde, LocalDateTime hasta);

    List<DeudaAlumnoResponse> reportarDeudas();

    PageResponse<AuditoriaReporteResponse> reportarAuditoria(
            Long codUsuario, String modulo, TipoOperacionAuditoria operacion, Instant desde, Instant hasta, Pageable pageable);
}
