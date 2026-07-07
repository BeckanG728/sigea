package com.institucion.sigea.aula.service.impl;

import com.institucion.sigea.auditoria.Auditable;
import com.institucion.sigea.aula.dto.request.AulaRequest;
import com.institucion.sigea.aula.dto.response.AulaBusquedaResponse;
import com.institucion.sigea.aula.dto.response.AulaResponse;
import com.institucion.sigea.aula.entity.Aula;
import com.institucion.sigea.aula.repository.AnioAcademicoRepository;
import com.institucion.sigea.aula.repository.AulaRepository;
import com.institucion.sigea.aula.repository.GradoRepository;
import com.institucion.sigea.aula.repository.NivelRepository;
import com.institucion.sigea.aula.service.AulaService;
import com.institucion.sigea.core.enums.TipoOperacionAuditoria;
import com.institucion.sigea.core.exception.BusinessException;
import com.institucion.sigea.core.exception.ErrorCode;
import com.institucion.sigea.parametro.service.ParametroService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AulaServiceImpl implements AulaService {

    private final AulaRepository aulaRepository;
    private final AnioAcademicoRepository anioAcademicoRepository;
    private final NivelRepository nivelRepository;
    private final GradoRepository gradoRepository;
    private final ParametroService parametroService;

    @Override
    @Transactional
    @Auditable(modulo = "aula", operacion = TipoOperacionAuditoria.INSERT)
    public AulaResponse crear(AulaRequest request) {
        boolean duplicada = aulaRepository.existsByAnioAcademicoIdAndNivelIdAndGradoIdAndSeccion(
                request.codAnioAcademico(), request.codNivel(), request.codGrado(), request.seccion());
        if (duplicada) {
            throw new BusinessException(ErrorCode.AULA_DUPLICADA, "Aula ya registrada",
                    Map.of("seccion", request.seccion()));
        }

        short capacidad = request.capacidadMaxima() != null
                ? request.capacidadMaxima()
                : Short.parseShort(parametroService.obtener("VACANTES_MAXIMAS_DEFAULT"));

        Aula aula = new Aula();
        aula.setAnioAcademico(anioAcademicoRepository.getReferenceById(request.codAnioAcademico()));
        aula.setNivel(nivelRepository.getReferenceById(request.codNivel()));
        aula.setGrado(gradoRepository.getReferenceById(request.codGrado()));
        aula.setSeccion(request.seccion());
        aula.setCapacidadMaxima(capacidad);

        aulaRepository.save(aula);
        return new AulaResponse(aula.getId(), aula.getSeccion(), aula.getCapacidadMaxima());
    }

    @Override
    public List<AulaBusquedaResponse> buscar(Long anioAcademicoId, Long nivelId) {
        return aulaRepository.buscar(anioAcademicoId, nivelId).stream()
                .map(this::toBusquedaResponse)
                .toList();
    }

    private AulaBusquedaResponse toBusquedaResponse(Aula aula) {
        String descripcion = "%d - %s - %s %s".formatted(
                aula.getAnioAcademico().getAnio(),
                aula.getNivel().getNombre(),
                aula.getGrado().getNombreGrado(),
                aula.getSeccion());
        // TODO (INT-1 con Persona 3): reemplazar capacidadMaxima por
        // capacidadMaxima - matriculadosActuales cuando exista Matricula.
        return new AulaBusquedaResponse(aula.getId(), descripcion, aula.getCapacidadMaxima());
    }
}