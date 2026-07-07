package com.institucion.sigea.matricula.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record MatriculaResponse(
        Long id,
        Integer codAlumno,
        Integer codAula,
        Integer codAnioAcademico,
        LocalDateTime fechaMatricula,
        List<CuotaResponse> cuotas
) {}
