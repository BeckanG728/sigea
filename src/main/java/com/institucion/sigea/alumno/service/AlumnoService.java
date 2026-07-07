package com.institucion.sigea.alumno.service;

import com.institucion.sigea.alumno.dto.request.AlumnoRequest;
import com.institucion.sigea.alumno.dto.response.AlumnoResponse;

public interface AlumnoService {
    AlumnoResponse crear(AlumnoRequest request);
}