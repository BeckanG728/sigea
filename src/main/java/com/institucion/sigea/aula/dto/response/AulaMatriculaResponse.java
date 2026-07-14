package com.institucion.sigea.aula.dto.response;

public record AulaMatriculaResponse(
        Long cod,
        String nivel,
        String grado,
        String seccion,
        long cupo,
        short max,
        boolean estado,
        Integer periodo
) {}