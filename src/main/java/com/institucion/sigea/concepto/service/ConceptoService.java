package com.institucion.sigea.concepto.service;

import com.institucion.sigea.concepto.dto.request.ConceptoRequest;
import com.institucion.sigea.concepto.dto.response.ConceptoResponse;

import java.util.List;

public interface ConceptoService {
    ConceptoResponse crear(ConceptoRequest request);
    ConceptoResponse actualizar(Long id, ConceptoRequest request);
    void eliminar(Long id);
    List<ConceptoResponse> listar();
}