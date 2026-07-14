package com.institucion.sigea.reporte.controller;

import com.institucion.sigea.matricula.entity.EstadoCuota;
import com.institucion.sigea.reporte.dto.response.DeudaDetalleReporteResponse;
import com.institucion.sigea.reporte.service.DeudaDetalleReporteService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/reportes")
@RequiredArgsConstructor
public class DeudaDetalleReporteController {

    private final DeudaDetalleReporteService deudaDetalleReporteService;

    /**
     * GET /api/reportes/deudas/detalle?anio=2026&estados=PENDIENTE&estados=BLOQUEADA
     * Ambos parámetros son opcionales.
     */
    @GetMapping("/deudas/detalle")
    @PreAuthorize("hasPermission(null, 'REPORTE', 'VER')")
    public List<DeudaDetalleReporteResponse> deudasDetalle(
            @RequestParam(required = false) Integer anio,
            @RequestParam(required = false) List<EstadoCuota> estados) {
        return deudaDetalleReporteService.reportar(anio, estados);
    }
}
