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
 * propósito: el login pasa por 2FA (login -> requiere2FA -> verify-2fa), un
 * flujo de dos pasos que no encaja en el {@code AuthenticationManager}
 * estándar de Spring Security. AuthService valida usuario/password a mano
 * con {@link PasswordEncoder#matches} contra UsuarioRepository.
 * <p>
 * TODO(usuarios-roles): si en algún punto se prefiere volver al flujo
 * estándar de Spring Security para el login, reintroducir aquí
 * DaoAuthenticationProvider + AuthenticationManager apuntando a un
 * CustomUserDetailsService respaldado por UsuarioRepository.
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
