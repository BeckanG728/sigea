package com.institucion.sigea.matricula.service;

import com.institucion.sigea.matricula.dto.request.MatriculaRequest;
import com.institucion.sigea.matricula.dto.response.MatriculaReporteResponse;
import com.institucion.sigea.matricula.dto.response.MatriculaResponse;

import java.util.List;

public interface MatriculaService {
    MatriculaResponse matricular(MatriculaRequest request);
    List<MatriculaReporteResponse> reportar(Integer anioAcademico, Long codNivel, Long codGrado, Integer codAula);
}