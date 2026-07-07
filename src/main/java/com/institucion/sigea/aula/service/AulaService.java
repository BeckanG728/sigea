package com.institucion.sigea.aula.service;

import com.institucion.sigea.aula.dto.request.AulaRequest;
import com.institucion.sigea.aula.dto.response.AulaResponse;

public interface AulaService {
    AulaResponse crear(AulaRequest request);
}