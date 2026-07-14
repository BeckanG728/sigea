package com.institucion.sigea.alumno.dto.response;

public record AlumnoMatriculaResponse(
        Long id,
        String documento,
        String paterno,
        String materno,
        String nombre,
        boolean estado
) {}