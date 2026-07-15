package com.institucion.sigea.security.filter;

import com.institucion.sigea.security.jwt.JwtPrincipal;
import com.institucion.sigea.security.jwt.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

import static com.institucion.sigea.config.CacheConfig.CACHE_USUARIOS_DESACTIVADOS;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final String ROLE_PREFIX = "ROLE_";

    private final JwtUtil jwtUtil;
    private final CacheManager cacheManager;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(7);

        try {
            // UNA SOLA LECTURA DEL TOKEN
            Claims claims = jwtUtil.parse(token);

            Long userId = jwtUtil.extractUserId(claims);

            if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                String username = jwtUtil.extractUsername(claims);
                String role = jwtUtil.extractRole(claims);

                List<GrantedAuthority> authorities = role == null
                        ? List.of()
                        : List.of(new SimpleGrantedAuthority(ROLE_PREFIX + role));

                // Verificar si el usuario fue desactivado (caché Caffeine, sin BD)
                Cache disabledCache = cacheManager.getCache(CACHE_USUARIOS_DESACTIVADOS);
                if (disabledCache != null && disabledCache.get(userId, Long.class) != null) {
                    SecurityContextHolder.clearContext();
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"error\":\"USER_DISABLED\",\"message\":\"Usuario desactivado\"}");
                    return;
                }

                JwtPrincipal principal = new JwtPrincipal(userId, username, role, jwtUtil.isTwoFactorVerified(claims));

                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(
                                principal,
                                null,
                                authorities
                        );

                auth.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                SecurityContextHolder.getContext().setAuthentication(auth);
            }

        } catch (JwtException | IllegalArgumentException e) {
            SecurityContextHolder.clearContext();
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"TOKEN_EXPIRED\",\"message\":\"Token inválido o expirado\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }
}
