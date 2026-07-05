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
 * Contiene solo lo que viaja en los claims del JWT ({@code userId},
 * {@code username}, {@code rol}). Si un endpoint necesita el registro
 * completo de {@code Usuario}, debe inyectar {@code UsuarioRepository}
 * y buscar por {@link #userId()} — no cargarlo aquí en el filtro para
 * no acoplar la autenticación a BD en cada request.
 */
public record JwtPrincipal(Long userId, String username, String rol, boolean twoFactorVerified) {
}
