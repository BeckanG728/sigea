package com.institucion.sigea.auth.controller;

import com.institucion.sigea.auth.dto.request.CambioPasswordRequest;
import com.institucion.sigea.auth.dto.response.Habilitar2FaResponse;
import com.institucion.sigea.auth.service.CambioPasswordService;
import com.institucion.sigea.auth.service.TotpService;
import com.institucion.sigea.auth.service.TwoFaEnrollmentService;
import com.institucion.sigea.core.exception.BusinessException;
import com.institucion.sigea.core.exception.ErrorCode;
import com.institucion.sigea.security.jwt.JwtPrincipal;
import com.institucion.sigea.usuario.entity.Usuario;
import com.institucion.sigea.usuario.repository.UsuarioRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
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
    private final TotpService totpService;
    private final UsuarioRepository usuarioRepository;

    @PutMapping("/change-password")
    public ResponseEntity<Map<String, String>> cambiarPassword(
            @Valid @RequestBody CambioPasswordRequest request) {
        cambioPasswordService.cambiarPassword(request);
        return ResponseEntity.ok(Map.of("message", "Contraseña actualizada correctamente"));
    }

    @PostMapping("/2fa/enable")
    public ResponseEntity<Habilitar2FaResponse> habilitar2fa() {
        return ResponseEntity.ok(twoFaEnrollmentService.habilitar2fa());
    }

    @GetMapping("/2fa/qr")
    public ResponseEntity<Map<String, String>> obtenerQr(Authentication authentication) {
        JwtPrincipal principal = (JwtPrincipal) authentication.getPrincipal();
        Usuario usuario = usuarioRepository.findById(principal.userId())
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.INVALID_CREDENTIALS, "Usuario no encontrado"));
        String qrUri = totpService.generarQrUri(
                usuario.getTotpSecret(), usuario.getEmail());
        return ResponseEntity.ok(Map.of("qrUri", qrUri));
    }

}
