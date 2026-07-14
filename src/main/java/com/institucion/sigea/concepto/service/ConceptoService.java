package com.institucion.sigea.concepto.service;

import com.institucion.sigea.concepto.dto.request.ConceptoRequest;
import com.institucion.sigea.concepto.dto.response.ConceptoResponse;
import com.institucion.sigea.core.api.PageResponse;
import org.springframework.data.domain.Pageable;

public interface ConceptoService {
    ConceptoResponse crear(ConceptoRequest request);
    ConceptoResponse actualizar(Long id, ConceptoRequest request);
    void eliminar(Long id);
    PageResponse<ConceptoResponse> listar(Long anioAcademicoId, Pageable pageable);
}