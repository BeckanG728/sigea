package com.institucion.sigea.dashboard.service;

import com.institucion.sigea.dashboard.dto.DashboardResponse;
import com.institucion.sigea.dashboard.dto.MatriculaDashboardResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface DashboardService {
    DashboardResponse obtenerResumen(Pageable pageable);
    List<MatriculaDashboardResponse> exportarMatriculas();
}
