package com.institucion.sigea.aula.service.impl;

import com.institucion.sigea.aula.entity.Grado;
import com.institucion.sigea.aula.repository.GradoRepository;
import com.institucion.sigea.aula.service.GradoService;
import com.institucion.sigea.auditoria.Auditable;
import com.institucion.sigea.core.enums.TipoOperacionAuditoria;
import com.institucion.sigea.core.exception.BusinessException;
import com.institucion.sigea.core.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GradoServiceImpl implements GradoService {

    private final GradoRepository gradoRepository;

    @Override
    public List<Grado> listar() {
        return gradoRepository.findAll();
    }

    @Override
    @Transactional
    @Auditable(modulo = "grado", operacion = TipoOperacionAuditoria.DELETE)
    public void eliminar(Long id) {
        Grado grado = gradoRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.GRADO_NO_ENCONTRADO, "Grado no encontrado"));
        try {
            gradoRepository.delete(grado);
            gradoRepository.flush();
        } catch (DataIntegrityViolationException e) {
            grado.setEstado(false);
            gradoRepository.save(grado);
        }
    }
}
