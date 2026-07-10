package com.institucion.sigea.reporte.service;

import com.institucion.sigea.reporte.dto.response.VacanteReporteResponse;
import java.util.List;

public interface VacanteReporteService {
    List<VacanteReporteResponse> reportar(Long anioAcademicoId, Long nivelId, Long gradoId);
}
