package com.institucion.sigea.concepto.dto.request;

import jakarta.validation.constraints.NotBlank;

public record TipoConceptoRequest(@NotBlank String nombre) {}