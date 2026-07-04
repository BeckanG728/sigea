package com.institucion.sigea.security.jwt;

/**
 * Principal autenticado extraído directamente de los claims del JWT.
 * <p>
 * No es un {@link org.springframework.security.core.userdetails.UserDetails}
 * a propósito: en autenticación 100% stateless no hace falta cargar
 * password/estado desde BD en cada request. Los controllers/servicios que
 * necesiten el usuario autenticado lo obtienen así:
 * <pre>
 *   JwtPrincipal principal = (JwtPrincipal) SecurityContextHolder
 *       .getContext().getAuthentication().getPrincipal();
 * </pre>
 * <p>
 * TODO(usuarios-roles): si en algún endpoint se necesita el registro
 * completo de Usuario (no solo id/username/rol), inyectar UsuarioRepository
 * en el servicio y buscar por {@link #userId()} — no cargarlo aquí en el
 * filtro para no acoplar la autenticación a BD en cada request.
 */
public record JwtPrincipal(Long userId, String username, boolean twoFactorVerified) {
}
