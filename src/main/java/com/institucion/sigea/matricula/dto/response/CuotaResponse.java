package com.institucion.sigea.matricula.dto.response;

import com.institucion.sigea.matricula.entity.EstadoCuota;

import java.math.BigDecimal;

public record CuotaResponse(
        Long id,
        Integer codConcepto,
        BigDecimal montoPagar,
        Short ordenPago,
        EstadoCuota estadoCuota
) {}
