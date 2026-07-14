package com.institucion.sigea.concepto.service.impl;

import com.institucion.sigea.auditoria.Auditable;
import com.institucion.sigea.aula.repository.AnioAcademicoRepository;
import com.institucion.sigea.concepto.dto.request.ConceptoRequest;
import com.institucion.sigea.concepto.dto.response.ConceptoResponse;
import com.institucion.sigea.concepto.entity.Concepto;
import com.institucion.sigea.concepto.mapper.ConceptoMapper;
import com.institucion.sigea.concepto.repository.ConceptoRepository;
import com.institucion.sigea.concepto.repository.TipoConceptoRepository;
import com.institucion.sigea.concepto.service.ConceptoService;
import com.institucion.sigea.core.api.PageResponse;
import com.institucion.sigea.core.enums.TipoOperacionAuditoria;
import com.institucion.sigea.core.exception.BusinessException;
import com.institucion.sigea.core.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    private final ConceptoMapper conceptoMapper;

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
        concepto.setTipo(request.tipo());

        conceptoRepository.save(concepto);
        return conceptoMapper.toResponse(concepto);
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
        if (request.codTipoConcepto() != null) {
            concepto.setTipoConcepto(tipoConceptoRepository.getReferenceById(request.codTipoConcepto()));
        }
        if (request.nombreConcepto() != null) {
            concepto.setNombreConcepto(request.nombreConcepto());
        }
        concepto.setMonto(request.monto());
        concepto.setOrdenPago(request.ordenPago());
        concepto.setObligatorio(request.obligatorio());
        concepto.setTipo(request.tipo());

        conceptoRepository.save(concepto);
        return conceptoMapper.toResponse(concepto);
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
    @Transactional(readOnly = true)
    public List<ConceptoResponse> listarPorAnio(Integer anio) {
        return anioAcademicoRepository.findAll().stream()
                .filter(a -> a.getAnio().equals(anio) && a.isEstado())
                .findFirst()
                .map(a -> conceptoRepository.findByAnioAcademicoId(a.getId()).stream()
                        .filter(Concepto::isEstado)
                        .map(conceptoMapper::toResponse)
                        .toList())
                .orElse(List.of());
    }

    @Override
    public PageResponse<ConceptoResponse> listar(Long anioAcademicoId, Pageable pageable) {
        Page<Concepto> page;
        if (anioAcademicoId != null) {
            page = conceptoRepository.findByAnioAcademicoIdAndEstadoTrue(anioAcademicoId, pageable);
        } else {
            page = conceptoRepository.findByEstadoTrue(pageable);
        }
        return PageResponse.of(page.map(conceptoMapper::toResponse));
    }
}
