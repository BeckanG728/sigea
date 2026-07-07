package com.institucion.sigea.matricula.service;

import com.institucion.sigea.matricula.dto.request.MatriculaRequest;
import com.institucion.sigea.matricula.dto.response.MatriculaResponse;

public interface MatriculaService {
    MatriculaResponse matricular(MatriculaRequest request);
}