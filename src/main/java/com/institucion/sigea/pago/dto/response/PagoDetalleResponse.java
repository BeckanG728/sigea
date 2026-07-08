package com.institucion.sigea.pago.dto.response;

import com.institucion.sigea.pago.entity.MedioPago;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PagoDetalleResponse(
        Long codPago,
        Long codCuota,
        BigDecimal montoPagado,
        MedioPago medioPago,
        LocalDateTime fechaPago
) {}
