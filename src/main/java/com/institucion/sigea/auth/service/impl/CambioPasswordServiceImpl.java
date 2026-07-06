package com.institucion.sigea.auth.service.impl;

import com.institucion.sigea.auth.dto.request.CambioPasswordRequest;
import com.institucion.sigea.auth.service.CambioPasswordService;
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
public class CambioPasswordServiceImpl implements CambioPasswordService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void cambiarPassword(CambioPasswordRequest request) {
        JwtPrincipal principal = (JwtPrincipal) SecurityContextHolder
                .getContext().getAuthentication().getPrincipal();

        Usuario usuario = usuarioRepository.findById(principal.userId())
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.INVALID_CREDENTIALS,
                        "Usuario no encontrado"));

        if (!passwordEncoder.matches(request.passwordActual(), usuario.getPassword())) {
            throw new BusinessException(
                    ErrorCode.INVALID_CREDENTIALS,
                    "Contraseña incorrecta");
        }

        usuario.setPassword(passwordEncoder.encode(request.passwordNueva()));
        usuarioRepository.save(usuario);
    }
}
