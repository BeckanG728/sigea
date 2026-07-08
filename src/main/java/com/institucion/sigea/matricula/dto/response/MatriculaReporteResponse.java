package com.institucion.sigea.matricula.dto.response;

import java.time.LocalDateTime;

public record MatriculaReporteResponse(
        Long id,
        Integer codAlumno,
        Integer codAula,
        Integer codAnioAcademico,
        LocalDateTime fechaMatricula
) {}
