package com.institucion.sigea.aula.service.impl;

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
    public List<AnioAcademico> listar() {
        return anioAcademicoRepository.findAll();
    }

    @Override
    @Transactional
    @Auditable(modulo = "anio_academico", operacion = TipoOperacionAuditoria.INSERT)
    public AnioAcademico crear(Integer anio) {
        AnioAcademico nuevo = new AnioAcademico();
        nuevo.setAnio(anio);
        nuevo.setEstado(true);

        anioAcademicoRepository.findByEstadoTrue().ifPresent(a -> a.setEstado(false));
        return anioAcademicoRepository.save(nuevo);
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
    public AnioAcademico obtenerActivo() {
        return anioAcademicoRepository.findByEstadoTrue()
                .orElseThrow(() -> new BusinessException(ErrorCode.INTERNAL_ERROR,
                        "No hay un año académico activo"));
    }
}
