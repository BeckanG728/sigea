package com.institucion.sigea.matricula.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record MatriculaRegisterRequest(
        @NotNull Long alumnoId,
        @NotNull Long aulaId,
        @NotNull Long anioId,
        @NotNull String codigoTotp,
        @NotEmpty List<Long> conceptosActivos
) {}