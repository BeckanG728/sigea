package com.institucion.sigea.concepto.service;

import com.institucion.sigea.concepto.dto.response.ClonadoResponse;

public interface ClonadorConceptoService {
    ClonadoResponse clonar(Long anioOrigen, Long anioDestino);
}