package com.institucion.sigea.pago.dto.request;

import com.institucion.sigea.pago.entity.MedioPago;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record RegistrarPagoRequest(
        @NotNull Long codCuota,
        @NotNull @Positive BigDecimal montoPagado,
        @NotNull MedioPago medioPago
) {}
