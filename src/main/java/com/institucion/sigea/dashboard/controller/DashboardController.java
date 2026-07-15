package com.institucion.sigea.dashboard.controller;

import com.institucion.sigea.dashboard.dto.DashboardResponse;
import com.institucion.sigea.dashboard.dto.MatriculaDashboardResponse;
import com.institucion.sigea.dashboard.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    @PreAuthorize("hasPermission(null, 'DASHBOARD', 'VER')")
    public DashboardResponse obtenerResumen(@PageableDefault(size = 5) Pageable pageable) {
        return dashboardService.obtenerResumen(pageable);
    }

    @GetMapping("/exportar")
    @PreAuthorize("hasPermission(null, 'DASHBOARD', 'VER')")
    public List<MatriculaDashboardResponse> exportar() {
        return dashboardService.exportarMatriculas();
    }
}
