package com.institucion.sigea.matricula.dto.request;

import jakarta.validation.constraints.NotNull;

public record MatriculaPreviewRequest(
        @NotNull Long alumnoId,
        @NotNull Long aulaId,
        @NotNull Long anioId
) {}