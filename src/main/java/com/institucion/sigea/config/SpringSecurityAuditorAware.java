package com.institucion.sigea.config;

import com.institucion.sigea.security.jwt.JwtPrincipal;
import com.institucion.sigea.usuario.entity.Usuario;
import com.institucion.sigea.usuario.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class SpringSecurityAuditorAware implements AuditorAware<Usuario> {

    private final UsuarioRepository usuarioRepository;

    @Override
    public Optional<Usuario> getCurrentAuditor() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof JwtPrincipal principal)) {
            return Optional.empty(); // seeders, tareas sin request HTTP, etc.
        }
        return Optional.of(usuarioRepository.getReferenceById(principal.userId()));
    }
}