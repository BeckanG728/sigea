package com.institucion.sigea.auth.service.impl;

import com.institucion.sigea.auth.dto.response.Habilitar2FaResponse;
import com.institucion.sigea.auth.service.TotpService;
import com.institucion.sigea.auth.service.TwoFaEnrollmentService;
import com.institucion.sigea.auditoria.Auditable;
import com.institucion.sigea.core.enums.TipoOperacionAuditoria;
import com.institucion.sigea.core.exception.BusinessException;
import com.institucion.sigea.core.exception.ErrorCode;
import com.institucion.sigea.security.jwt.JwtPrincipal;
import com.institucion.sigea.usuario.entity.Usuario;
import com.institucion.sigea.usuario.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TwoFaEnrollmentServiceImpl implements TwoFaEnrollmentService {

    private final UsuarioRepository usuarioRepository;
    private final TotpService totpService;

    @Override
    @Transactional
    @Auditable(modulo = "auth", operacion = TipoOperacionAuditoria.UPDATE)
    public Habilitar2FaResponse habilitar2fa() {
        JwtPrincipal principal = (JwtPrincipal) SecurityContextHolder
                .getContext().getAuthentication().getPrincipal();

        Usuario usuario = usuarioRepository.findById(principal.userId())
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.INVALID_CREDENTIALS,
                        "Usuario no encontrado"));

        if (!usuario.isTotpVerificado()) {
            String qrUri = totpService.generarQrUri(
                    usuario.getTotpSecret(), usuario.getNombreUsuario());
            return new Habilitar2FaResponse(qrUri, false);
        }

        if (!usuario.isDosFactorHabilitado()) {
            usuario.setDosFactorHabilitado(true);
            usuarioRepository.save(usuario);
            return new Habilitar2FaResponse(null, true);
        }

        throw new BusinessException(ErrorCode.TWOFA_ALREADY_VERIFIED,
                "La autenticación de doble factor ya se encuentra activa.");
    }
}
