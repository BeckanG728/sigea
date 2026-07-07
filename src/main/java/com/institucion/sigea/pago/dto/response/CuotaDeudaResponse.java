package com.institucion.sigea.pago.dto.response;

import com.institucion.sigea.matricula.entity.EstadoCuota;

import java.math.BigDecimal;

public record CuotaDeudaResponse(
        Long codCuota,
        Integer codMatricula,
        BigDecimal montoPagar,
        Short ordenPago,
        EstadoCuota estadoCuota
) {}
