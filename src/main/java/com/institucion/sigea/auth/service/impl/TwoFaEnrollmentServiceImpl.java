package com.institucion.sigea.auth.service.impl;

import com.institucion.sigea.auth.dto.internal.GenerarSecretoResult;
import com.institucion.sigea.auth.dto.request.Habilitar2FaRequest;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TwoFaEnrollmentServiceImpl implements TwoFaEnrollmentService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final TotpService totpService;

    @Override
    @Transactional
    @Auditable(modulo = "auth", operacion = TipoOperacionAuditoria.UPDATE)
    public Habilitar2FaResponse habilitar2fa(Habilitar2FaRequest request) {
        JwtPrincipal principal = (JwtPrincipal) SecurityContextHolder
                .getContext().getAuthentication().getPrincipal();

        Usuario usuario = usuarioRepository.findById(principal.userId())
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.INVALID_CREDENTIALS,
                        "Usuario no encontrado"));

        if (!passwordEncoder.matches(request.password(), usuario.getPassword())) {
            throw new BusinessException(
                    ErrorCode.INVALID_CREDENTIALS,
                    "Contraseña incorrecta");
        }

        GenerarSecretoResult secreto = totpService.generarSecreto(usuario.getUsername());

        usuario.setTotpSecret(secreto.secretRaw());
        usuario.setDosFactorHabilitado(true);
        usuarioRepository.save(usuario);

        return new Habilitar2FaResponse(secreto.uri(), true);
    }
}
