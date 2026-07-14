package com.institucion.sigea.aula.service.impl;

import com.institucion.sigea.aula.dto.response.AnioAcademicoResponse;
import com.institucion.sigea.aula.entity.AnioAcademico;
import com.institucion.sigea.aula.repository.AnioAcademicoRepository;
import com.institucion.sigea.aula.service.AnioAcademicoService;
import com.institucion.sigea.auditoria.Auditable;
import com.institucion.sigea.core.enums.TipoOperacionAuditoria;
import com.institucion.sigea.core.exception.BusinessException;
import com.institucion.sigea.core.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AnioAcademicoServiceImpl implements AnioAcademicoService {

    private final AnioAcademicoRepository anioAcademicoRepository;

    @Override
    @Transactional(readOnly = true)
    public List<AnioAcademicoResponse> listar() {
        return anioAcademicoRepository.findAll().stream()
                .map(a -> new AnioAcademicoResponse(a.getId(), a.getAnio(), a.isEstado()))
                .toList();
    }

    @Override
    @Transactional
    @Auditable(modulo = "anio_academico", operacion = TipoOperacionAuditoria.INSERT)
    public AnioAcademicoResponse crear(Integer anio) {
        AnioAcademico nuevo = new AnioAcademico();
        nuevo.setAnio(anio);
        nuevo.setEstado(true);

        anioAcademicoRepository.findByEstadoTrue().ifPresent(a -> a.setEstado(false));
        AnioAcademico guardado = anioAcademicoRepository.save(nuevo);
        return new AnioAcademicoResponse(guardado.getId(), guardado.getAnio(), guardado.isEstado());
    }

    @Override
    @Transactional
    @Auditable(modulo = "anio_academico", operacion = TipoOperacionAuditoria.UPDATE)
    public void activar(Long id) {
        // Buscar año que actualmente esté activo
        List<AnioAcademico> activos = anioAcademicoRepository.findAll().stream()
                .filter(a -> a.isEstado() && !a.getId().equals(id))
                .toList();
        // Desactivar todos
        activos.forEach(a -> a.setEstado(false));
        // Activar el solicitado
        AnioAcademico target = anioAcademicoRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.INTERNAL_ERROR, "Año no encontrado"));
        target.setEstado(true);
    }
    @Override
    @Transactional(readOnly = true)
    public AnioAcademicoResponse obtenerActivo() {
        AnioAcademico a = anioAcademicoRepository.findByEstadoTrue()
                .orElseThrow(() -> new BusinessException(ErrorCode.INTERNAL_ERROR,
                        "No hay un año académico activo"));
        return new AnioAcademicoResponse(a.getId(), a.getAnio(), a.isEstado());
    }
}
