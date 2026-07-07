package com.institucion.sigea.concepto.dto.request;

import jakarta.validation.constraints.NotNull;

public record ClonadoRequest(
        @NotNull Long anioOrigen,
        @NotNull Long anioDestino
) {}