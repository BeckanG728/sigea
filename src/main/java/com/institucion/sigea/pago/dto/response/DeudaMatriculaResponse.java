package com.institucion.sigea.pago.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DeudaMatriculaResponse(
        String concepto,
        BigDecimal monto,
        LocalDate fecha,
        String estado,
        String recibo
) {}