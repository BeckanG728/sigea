package com.institucion.sigea.concepto.service.impl;

import com.institucion.sigea.auditoria.Auditable;
import com.institucion.sigea.aula.repository.AnioAcademicoRepository;
import com.institucion.sigea.concepto.dto.request.ConceptoRequest;
import com.institucion.sigea.concepto.dto.response.ConceptoResponse;
import com.institucion.sigea.concepto.entity.Concepto;
import com.institucion.sigea.concepto.repository.ConceptoRepository;
import com.institucion.sigea.concepto.repository.TipoConceptoRepository;
import com.institucion.sigea.concepto.service.ConceptoService;
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
public class ConceptoServiceImpl implements ConceptoService {

    private final ConceptoRepository conceptoRepository;
    private final AnioAcademicoRepository anioAcademicoRepository;
    private final TipoConceptoRepository tipoConceptoRepository;

    @Override
    @Transactional
    @Auditable(modulo = "concepto", operacion = TipoOperacionAuditoria.INSERT)
    public ConceptoResponse crear(ConceptoRequest request) {
        boolean duplicado = conceptoRepository
                .existsByAnioAcademicoIdAndNombreConcepto(request.codAnioAcademico(), request.nombreConcepto());
        if (duplicado) {
            throw new BusinessException(ErrorCode.CONCEPTO_DUPLICADO, "Concepto ya existe",
                    Map.of("nombreConcepto", request.nombreConcepto()));
        }
        Concepto concepto = new Concepto();
        concepto.setAnioAcademico(anioAcademicoRepository.getReferenceById(request.codAnioAcademico()));
        concepto.setTipoConcepto(tipoConceptoRepository.getReferenceById(request.codTipoConcepto()));
        concepto.setNombreConcepto(request.nombreConcepto());
        concepto.setMonto(request.monto());
        concepto.setOrdenPago(request.ordenPago());
        concepto.setObligatorio(request.obligatorio());

        conceptoRepository.save(concepto);
        return toResponse(concepto);
    }

    @Override
    @Transactional
    @Auditable(modulo = "concepto", operacion = TipoOperacionAuditoria.UPDATE)
    public ConceptoResponse actualizar(Long id, ConceptoRequest request) {
        Concepto concepto = conceptoRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.INTERNAL_ERROR, "Concepto no encontrado"));

        if (!concepto.getVersion().equals(request.version())) {
            throw new BusinessException(ErrorCode.VERSION_CONFLICT, "Edición concurrente detectada",
                    Map.of("versionActual", concepto.getVersion(), "versionEnviada", request.version()));
        }
        concepto.setMonto(request.monto());
        concepto.setOrdenPago(request.ordenPago());
        concepto.setObligatorio(request.obligatorio());

        conceptoRepository.save(concepto);
        return toResponse(concepto);
    }

    private ConceptoResponse toResponse(Concepto concepto) {
        return new ConceptoResponse(
                concepto.getId(),
                concepto.getNombreConcepto(),
                concepto.getMonto(),
                concepto.getOrdenPago(),
                concepto.isObligatorio(),
                concepto.getVersion());
    }

    @Override
    @Transactional
    @Auditable(modulo = "concepto", operacion = TipoOperacionAuditoria.DELETE)
    public void eliminar(Long id) {
        Concepto concepto = conceptoRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.CONCEPTO_NO_ENCONTRADO, "Concepto no encontrado"));
        concepto.setEstado(false);
        conceptoRepository.save(concepto);
    }

    @Override
    public List<ConceptoResponse> listar() {
        return conceptoRepository.findAll().stream()
                .filter(Concepto::isEstado)
                .map(this::toResponse)
                .toList();
    }
}
