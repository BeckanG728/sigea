package com.institucion.sigea.concepto.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record ConceptoRequest(
        @NotNull Long codAnioAcademico,
        @NotNull Long codTipoConcepto,
        @NotBlank String nombreConcepto,
        @Positive BigDecimal monto,
        @NotNull Short ordenPago,
        @NotNull Boolean obligatorio,
        Long version // null al crear; obligatorio al actualizar
) {}