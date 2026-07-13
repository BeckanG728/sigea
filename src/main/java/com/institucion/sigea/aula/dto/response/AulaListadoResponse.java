package com.institucion.sigea.aula.dto.response;

public record AulaListadoResponse(
        Long id,
        String codigo,
        String nivel,
        String grado,
        String seccion,
        short capacidadMaxima,
        long matriculados,
        long vacantes,
        boolean estado
) {}
