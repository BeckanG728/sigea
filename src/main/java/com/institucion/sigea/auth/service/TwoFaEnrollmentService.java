package com.institucion.sigea.auth.service;

import com.institucion.sigea.auth.dto.request.Habilitar2FaRequest;
import com.institucion.sigea.auth.dto.response.Habilitar2FaResponse;

public interface TwoFaEnrollmentService {

    Habilitar2FaResponse habilitar2fa(Habilitar2FaRequest request);
}
