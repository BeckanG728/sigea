package com.institucion.sigea.aula.service;

import com.institucion.sigea.aula.dto.request.AulaRequest;
import com.institucion.sigea.aula.dto.response.AulaBusquedaResponse;
import com.institucion.sigea.aula.dto.response.AulaResponse;

import java.util.List;

public interface AulaService {
    AulaResponse crear(AulaRequest request);
    List<AulaBusquedaResponse> buscar(Long anioAcademicoId, Long nivelId);
}