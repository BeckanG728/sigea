package com.institucion.sigea.auth.service;

import com.institucion.sigea.auth.dto.request.CambioPasswordRequest;

public interface CambioPasswordService {

    void cambiarPassword(CambioPasswordRequest request);
}
