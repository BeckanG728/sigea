package com.institucion.sigea.reporte.controller;

import com.institucion.sigea.reporte.dto.response.CajaReporteResponse;
import com.institucion.sigea.reporte.service.CajaReporteService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/reportes")
@RequiredArgsConstructor
public class CajaReporteController {

    private final CajaReporteService cajaReporteService;

    /**
     * GET /api/reportes/caja?desde=2026-01-01T00:00:00&hasta=2026-06-30T23:59:59
     * Mismo formato ISO que /reportes/pagos.
     */
    @GetMapping("/caja")
    @PreAuthorize("hasPermission(null, 'REPORTE', 'VER')")
    public CajaReporteResponse caja(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta) {
        return cajaReporteService.reportar(desde, hasta);
    }
}
