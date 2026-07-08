package com.institucion.sigea.reporte.service.impl;

import com.institucion.sigea.auditoria.AuditoriaEntity;
import com.institucion.sigea.auditoria.AuditoriaService;
import com.institucion.sigea.matricula.dto.response.MatriculaReporteResponse;
import com.institucion.sigea.matricula.service.MatriculaService;
import com.institucion.sigea.pago.dto.response.DeudaAlumnoResponse;
import com.institucion.sigea.pago.dto.response.PagoReporteResponse;
import com.institucion.sigea.pago.service.PagoService;
import com.institucion.sigea.reporte.dto.response.AuditoriaReporteResponse;
import com.institucion.sigea.reporte.service.ReporteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReporteServiceImpl implements ReporteService {

    private final MatriculaService matriculaService;
    private final PagoService pagoService;
    private final AuditoriaService auditoriaService;

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
    public List<AuditoriaReporteResponse> reportarAuditoria(
            Long codUsuario, String modulo, Instant desde, Instant hasta) {
        return auditoriaService.buscar(codUsuario, modulo, desde, hasta).stream()
                .map(this::toAuditoriaResponse)
                .toList();
    }

    private AuditoriaReporteResponse toAuditoriaResponse(AuditoriaEntity a) {
        return new AuditoriaReporteResponse(
                a.getId(),
                a.getUsuario() != null ? a.getUsuario().getId() : null,
                a.getModulo(),
                a.getOperacion(),
                a.getCodigoRegistro(),
                a.getFechaHora(),
                a.getIpOrigen());
    }
}
