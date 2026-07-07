package com.institucion.sigea.concepto.service;

import com.institucion.sigea.concepto.dto.request.TipoConceptoRequest;
import com.institucion.sigea.concepto.dto.response.TipoConceptoResponse;

import java.util.List;

public interface TipoConceptoService {
    TipoConceptoResponse crear(TipoConceptoRequest request);
    List<TipoConceptoResponse> listar();
    TipoConceptoResponse actualizar(Long id, TipoConceptoRequest request);
    void eliminar(Long id);
}