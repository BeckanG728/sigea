package com.institucion.sigea.aula.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AulaRequest(
        @NotNull Long codAnioAcademico,
        @NotNull Long codNivel,
        @NotNull Long codGrado,
        @NotBlank String seccion,
        @Positive Short capacidadMaxima // opcional: si viene null, se usa el parámetro por defecto
) {}