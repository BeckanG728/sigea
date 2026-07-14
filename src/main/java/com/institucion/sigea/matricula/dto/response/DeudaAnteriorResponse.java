package com.institucion.sigea.matricula.dto.response;

import java.math.BigDecimal;

public record DeudaAnteriorResponse(
        String concepto,
        BigDecimal monto,
        String estado
) {}
