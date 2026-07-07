package com.institucion.sigea.concepto.service.impl;

import com.institucion.sigea.auditoria.Auditable;
import com.institucion.sigea.concepto.dto.request.TipoConceptoRequest;
import com.institucion.sigea.concepto.dto.response.TipoConceptoResponse;
import com.institucion.sigea.concepto.entity.TipoConcepto;
import com.institucion.sigea.concepto.repository.TipoConceptoRepository;
import com.institucion.sigea.concepto.service.TipoConceptoService;
import com.institucion.sigea.core.enums.TipoOperacionAuditoria;
import com.institucion.sigea.core.exception.BusinessException;
import com.institucion.sigea.core.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TipoConceptoServiceImpl implements TipoConceptoService {

    private final TipoConceptoRepository tipoConceptoRepository;

    @Override
    @Transactional
    @Auditable(modulo = "tipo_concepto", operacion = TipoOperacionAuditoria.INSERT)
    public TipoConceptoResponse crear(TipoConceptoRequest request) {
        if (tipoConceptoRepository.existsByNombre(request.nombre())) {
            throw new BusinessException(ErrorCode.TIPO_CONCEPTO_DUPLICADO, "Ya existe ese tipo de concepto",
                    Map.of("nombre", request.nombre()));
        }
        TipoConcepto tipoConcepto = new TipoConcepto();
        tipoConcepto.setNombre(request.nombre());
        tipoConceptoRepository.save(tipoConcepto);
        return toResponse(tipoConcepto);
    }

    @Override
    public List<TipoConceptoResponse> listar() {
        return tipoConceptoRepository.findByEstadoTrue().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    @Auditable(modulo = "tipo_concepto", operacion = TipoOperacionAuditoria.UPDATE)
    public TipoConceptoResponse actualizar(Long id, TipoConceptoRequest request) {
        TipoConcepto tipoConcepto = tipoConceptoRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.TIPO_CONCEPTO_NO_ENCONTRADO, "No encontrado"));
        tipoConcepto.setNombre(request.nombre());
        tipoConceptoRepository.save(tipoConcepto);
        return toResponse(tipoConcepto);
    }

    @Override
    @Transactional
    @Auditable(modulo = "tipo_concepto", operacion = TipoOperacionAuditoria.DELETE)
    public void eliminar(Long id) {
        TipoConcepto tipoConcepto = tipoConceptoRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.TIPO_CONCEPTO_NO_ENCONTRADO, "No encontrado"));
        tipoConcepto.setEstado(false); // eliminación lógica, nunca deleteById()
        tipoConceptoRepository.save(tipoConcepto);
    }

    private TipoConceptoResponse toResponse(TipoConcepto tipoConcepto) {
        return new TipoConceptoResponse(tipoConcepto.getId(), tipoConcepto.getNombre(), tipoConcepto.isEstado());
    }
}