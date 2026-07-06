package com.institucion.sigea.auth.service;

import com.institucion.sigea.auth.dto.request.LoginRequest;
import com.institucion.sigea.auth.dto.request.Verify2faRequest;
import com.institucion.sigea.auth.dto.response.LoginResponse;

public interface AuthService {

    LoginResponse login(LoginRequest request);

    LoginResponse verify2fa(Verify2faRequest request);
}
