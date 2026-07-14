package com.institucion.sigea.reporte.service.impl;

import com.institucion.sigea.auditoria.AuditoriaService;
import com.institucion.sigea.core.api.PageResponse;
import com.institucion.sigea.core.enums.TipoOperacionAuditoria;
import com.institucion.sigea.matricula.dto.response.MatriculaReporteResponse;
import com.institucion.sigea.matricula.service.MatriculaService;
import com.institucion.sigea.pago.dto.response.DeudaAlumnoResponse;
import com.institucion.sigea.pago.dto.response.PagoReporteResponse;
import com.institucion.sigea.pago.service.PagoService;
import com.institucion.sigea.reporte.dto.response.AuditoriaReporteResponse;
import com.institucion.sigea.reporte.mapper.AuditoriaMapper;
import com.institucion.sigea.reporte.service.ReporteService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReporteServiceImpl implements ReporteService {

    private final MatriculaService matriculaService;
    private final PagoService pagoService;
    private final AuditoriaService auditoriaService;

    private final AuditoriaMapper auditoriaMapper;

    @Override
    @Transactional(readOnly = true)
    public List<MatriculaReporteResponse> reportarMatriculas(
            Integer anioAcademico, Long codNivel, Long codGrado, Integer codAula) {
        return matriculaService.reportar(anioAcademico, codNivel, codGrado, codAula);
    }

    @Override
    @Transactional(readOnly = true)
    public PagoReporteResponse reportarPagos(LocalDateTime desde, LocalDateTime hasta) {
        return pagoService.reportarPagos(desde, hasta);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DeudaAlumnoResponse> reportarDeudas() {
        return pagoService.reportarDeudasConsolidadas();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AuditoriaReporteResponse> reportarAuditoria(
            Long codUsuario, String modulo, TipoOperacionAuditoria operacion,LocalDateTime desde, LocalDateTime hasta, Pageable pageable) {
        Instant desdeInstant = desde != null ? desde.atZone(ZoneId.systemDefault()).toInstant() : null;
        Instant hastaInstant = hasta != null ? hasta.atZone(ZoneId.systemDefault()).toInstant() : null;

    return PageResponse.of(
                auditoriaService.buscar(codUsuario, modulo, operacion, desdeInstant, hastaInstant, pageable)
                        .map(auditoriaMapper::toResponse));
    }
}
