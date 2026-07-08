package com.institucion.sigea.pago.service;

import com.institucion.sigea.pago.dto.request.RegistrarPagoRequest;
import com.institucion.sigea.pago.dto.response.PagoResponse;

public interface PagoTransaccionService {
    PagoResponse registrarPago(RegistrarPagoRequest request);
}
