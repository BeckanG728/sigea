package com.institucion.sigea.matricula.dto.request;

import jakarta.validation.constraints.NotNull;

public record MatriculaRequest(
        @NotNull Long codAlumno,
        @NotNull Long codAula,
        @NotNull Long codAnioAcademico
) {}
