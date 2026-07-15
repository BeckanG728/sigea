package com.institucion.sigea.reporte.service.impl;

import com.institucion.sigea.aula.entity.Aula;
import com.institucion.sigea.aula.repository.AulaRepository;
import com.institucion.sigea.matricula.repository.MatriculaRepository;
import com.institucion.sigea.reporte.dto.response.VacanteReporteResponse;
import com.institucion.sigea.reporte.service.VacanteReporteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VacanteReporteServiceImpl implements VacanteReporteService {

    private final AulaRepository aulaRepository;
    private final MatriculaRepository matriculaRepository;

    @Override
    @Transactional(readOnly = true)
    public List<VacanteReporteResponse> reportar(Long anioAcademicoId, Long nivelId, Long gradoId) {
        return aulaRepository.buscar(anioAcademicoId, nivelId).stream()
                .filter(aula -> gradoId == null || aula.getGrado().getId().equals(gradoId))
                .map(this::toVacanteResponse)
                .toList();
    }

    private VacanteReporteResponse toVacanteResponse(Aula aula) {
        long matriculados = matriculaRepository
                .countByCodAulaAndCodAnioAcademicoAndEstadoTrue(
                        aula.getId().intValue(),
                        aula.getAnioAcademico().getId().intValue());
        long vacantes = aula.getCapacidadMaxima() - matriculados;

        String descripcion = aula.getAnioAcademico().getAnio() + " - "
                + aula.getNivel().getNombre() + " - "
                + aula.getGrado().getNombreGrado() + " "
                + aula.getSeccion();

        return new VacanteReporteResponse(
                aula.getId(),
                descripcion,
                aula.getAnioAcademico().getAnio(),
                aula.getNivel().getNombre(),
                aula.getGrado().getNombreGrado(),
                aula.getSeccion(),
                aula.getCapacidadMaxima(),
                matriculados,
                Math.max(vacantes, 0));
    }
}
