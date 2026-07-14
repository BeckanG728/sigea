package com.institucion.sigea.matricula.service;

import com.institucion.sigea.matricula.dto.request.MatriculaPreviewRequest;
import com.institucion.sigea.matricula.dto.request.MatriculaRegisterRequest;
import com.institucion.sigea.matricula.dto.response.MatriculaPreviewResponse;
import com.institucion.sigea.matricula.dto.response.MatriculaRegisterResponse;

public interface MatriculaModuleService {
    MatriculaPreviewResponse preview(MatriculaPreviewRequest request);
    MatriculaRegisterResponse register(MatriculaRegisterRequest request);
}