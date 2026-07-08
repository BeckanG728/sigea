package com.institucion.sigea.pago.dto.response;

import com.institucion.sigea.pago.entity.MedioPago;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PagoResponse(
        Long codPago,
        Long codCuota,
        String numeroRecibo,
        BigDecimal montoPagado,
        MedioPago medioPago,
        LocalDateTime fechaPago
) {}
