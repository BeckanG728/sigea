package com.institucion.sigea.auth.controller;

import com.institucion.sigea.auth.dto.request.LoginRequest;
import com.institucion.sigea.auth.dto.request.Verify2faRequest;
import com.institucion.sigea.auth.dto.response.LoginResponse;
import com.institucion.sigea.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/login/verify-2fa")
    public ResponseEntity<LoginResponse> verify2fa(@Valid @RequestBody Verify2faRequest request) {
        return ResponseEntity.ok(authService.verify2fa(request));
    }

    @PostMapping("/verify-2fa")
    public ResponseEntity<LoginResponse> verify2faAlias(@Valid @RequestBody Verify2faRequest request) {
        return ResponseEntity.ok(authService.verify2fa(request));
    }
}
