package com.institucion.sigea.security.config;

import com.institucion.sigea.security.filter.JwtAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Sin {@code DaoAuthenticationProvider}/{@code AuthenticationManager} a
 * propósito. Decisión de equipo (cerrada, no reabrir):
 * <ul>
 *   <li>El login en dos pasos (login → {@code requiere2FA} → verify-2fa)
 *       no encaja en el contrato todo-o-nada de
 *       {@code DaoAuthenticationProvider}.</li>
 *   <li>Las autorizaciones no se resuelven vía {@code GrantedAuthority};
 *       {@code PermisoEvaluator} decide contra {@code Rol_Funcionalidad}.</li>
 *   <li>El rol viaja solo como claim informativo del JWT.</li>
 * </ul>
 * {@code AuthServiceImpl} valida credenciales contra
 * {@link PasswordEncoder#matches} / {@code UsuarioRepository} y emite
 * el JWT directamente, sin intermediarios de Spring Security.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/login", "/auth/login/verify-2fa",
                                "/health", "/actuator/health")
                        .permitAll()
                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
