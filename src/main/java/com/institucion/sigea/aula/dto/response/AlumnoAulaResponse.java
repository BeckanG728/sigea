package com.institucion.sigea.aula.dto.response;

public record AlumnoAulaResponse(
        String codigo,
        String nombreCompleto,
        String fechaMatricula,
        boolean estadoMatricula
) {}
