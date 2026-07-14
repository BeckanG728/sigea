package com.institucion.sigea.matricula.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ObligacionResponse(
        Long id,
        Long conceptoId,
        String nombreConcepto,
        BigDecimal monto,
        String estado,
        LocalDate fechaVencimiento,
        short ordenPago,
        BigDecimal saldoPendiente
) {}