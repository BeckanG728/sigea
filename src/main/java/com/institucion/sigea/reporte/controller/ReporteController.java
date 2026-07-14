package com.institucion.sigea.reporte.controller;

import com.institucion.sigea.core.api.PageResponse;
import com.institucion.sigea.core.enums.TipoOperacionAuditoria;
import com.institucion.sigea.matricula.dto.response.MatriculaReporteResponse;
import com.institucion.sigea.pago.dto.response.DeudaAlumnoResponse;
import com.institucion.sigea.pago.dto.response.PagoReporteResponse;
import com.institucion.sigea.reporte.dto.response.AuditoriaReporteResponse;
import com.institucion.sigea.reporte.service.ReporteService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/reportes") // sin /api: el context-path global ya lo agrega
public class ReporteController {

    private final ReporteService reporteService;

    public ReporteController(ReporteService reporteService) {
        this.reporteService = reporteService;
    }

    @GetMapping("/matriculas")
    @PreAuthorize("hasPermission(null, 'REPORTE', 'VER')")
    public List<MatriculaReporteResponse> matriculas(
            @RequestParam(required = false) Integer anioAcademico,
            @RequestParam(required = false) Long codNivel,
            @RequestParam(required = false) Long codGrado,
            @RequestParam(required = false) Integer codAula) {
        return reporteService.reportarMatriculas(anioAcademico, codNivel, codGrado, codAula);
    }

    @GetMapping("/pagos")
    @PreAuthorize("hasPermission(null, 'REPORTE', 'VER')")
    public PagoReporteResponse pagos(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta) {
        return reporteService.reportarPagos(desde, hasta);
    }

    @GetMapping("/deudas")
    @PreAuthorize("hasPermission(null, 'REPORTE', 'VER')")
    public List<DeudaAlumnoResponse> deudas() {
        return reporteService.reportarDeudas();
    }

    @GetMapping("/auditoria")
    @PreAuthorize("hasPermission(null, 'REPORTE', 'VER')")
    public PageResponse<AuditoriaReporteResponse> auditoria(
            @RequestParam(required = false) Long codUsuario,
            @RequestParam(required = false) String modulo,
            @RequestParam(required = false) TipoOperacionAuditoria operacion,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant hasta,
            @PageableDefault(size = 5, sort = "fechaHora", direction = Sort.Direction.DESC) Pageable pageable) {
        return reporteService.reportarAuditoria(codUsuario, modulo, operacion, desde, hasta, pageable);
    }
}
