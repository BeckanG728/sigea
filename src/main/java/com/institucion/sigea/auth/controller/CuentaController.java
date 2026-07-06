package com.institucion.sigea.auth.controller;

import com.institucion.sigea.auth.dto.request.CambioPasswordRequest;
import com.institucion.sigea.auth.dto.request.Habilitar2FaRequest;
import com.institucion.sigea.auth.dto.response.Habilitar2FaResponse;
import com.institucion.sigea.auth.service.CambioPasswordService;
import com.institucion.sigea.auth.service.TwoFaEnrollmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class CuentaController {

    private final CambioPasswordService cambioPasswordService;
    private final TwoFaEnrollmentService twoFaEnrollmentService;

    @PutMapping("/change-password")
    public ResponseEntity<Map<String, String>> cambiarPassword(
            @Valid @RequestBody CambioPasswordRequest request) {
        cambioPasswordService.cambiarPassword(request);
        return ResponseEntity.ok(Map.of("message", "Contraseña actualizada correctamente"));
    }

    @PostMapping("/2fa/enable")
    public ResponseEntity<Habilitar2FaResponse> habilitar2fa(
            @Valid @RequestBody Habilitar2FaRequest request) {
        return ResponseEntity.ok(twoFaEnrollmentService.habilitar2fa(request));
    }
}
