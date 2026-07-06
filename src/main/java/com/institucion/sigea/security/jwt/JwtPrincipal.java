package com.institucion.sigea.security.jwt;

public record JwtPrincipal(Long userId, String username, String rol, boolean twoFactorVerified) {
}
