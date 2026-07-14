package com.institucion.sigea.reporte.controller;

import com.institucion.sigea.reporte.dto.response.VacanteReporteResponse;
import com.institucion.sigea.reporte.service.VacanteReporteService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reportes")
@RequiredArgsConstructor
public class VacanteReporteController {

    private final VacanteReporteService vacanteReporteService;

    @GetMapping("/vacantes")
    @PreAuthorize("hasPermission(null, 'REPORTE_VACANTES', 'VER')")
    public List<VacanteReporteResponse> vacantes(
            @RequestParam(required = false) Long anioAcademico,
            @RequestParam(required = false) Long nivel,
            @RequestParam(required = false) Long grado) {
        return vacanteReporteService.reportar(anioAcademico, nivel, grado);
    }
}
