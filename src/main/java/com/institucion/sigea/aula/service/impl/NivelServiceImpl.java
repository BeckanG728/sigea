package com.institucion.sigea.aula.service.impl;

import com.institucion.sigea.aula.dto.response.NivelResponse;
import com.institucion.sigea.aula.entity.Nivel;
import com.institucion.sigea.aula.repository.NivelRepository;
import com.institucion.sigea.aula.service.NivelService;
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
public class NivelServiceImpl implements NivelService {

    private final NivelRepository nivelRepository;

    @Override
    public List<NivelResponse> listar() {
        return nivelRepository.findAll().stream()
                .map(n -> new NivelResponse(n.getId(), n.getNombre()))
                .toList();
    }

    @Override
    @Transactional
    @Auditable(modulo = "nivel", operacion = TipoOperacionAuditoria.DELETE)
    public void eliminar(Long id) {
        Nivel nivel = nivelRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NIVEL_NO_ENCONTRADO, "Nivel no encontrado"));
        try {
            nivelRepository.delete(nivel);
            nivelRepository.flush();
        } catch (DataIntegrityViolationException e) {
            nivel.setEstado(false);
            nivelRepository.save(nivel);
        }
    }
}
