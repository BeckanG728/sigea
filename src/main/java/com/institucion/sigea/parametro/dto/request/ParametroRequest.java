package com.institucion.sigea.parametro.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ParametroRequest(@NotBlank String valor) {}