package com.institucion.sigea.auth.service.impl;

import com.institucion.sigea.auth.dto.request.LoginRequest;
import com.institucion.sigea.auth.dto.request.Verify2faRequest;
import com.institucion.sigea.auth.dto.response.LoginResponse;
import com.institucion.sigea.auth.service.AuthService;
import com.institucion.sigea.auth.service.TotpService;
import com.institucion.sigea.auditoria.AuditoriaService;
import com.institucion.sigea.config.CacheConfig;
import com.institucion.sigea.config.properties.JwtProperties;
import com.institucion.sigea.core.exception.BusinessException;
import com.institucion.sigea.core.exception.ErrorCode;
import com.institucion.sigea.security.jwt.JwtUtil;
import com.institucion.sigea.usuario.entity.Usuario;
import com.institucion.sigea.usuario.repository.UsuarioRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final int MAX_INTENTOS_FALLIDOS = 5;
    private static final int VENTANA_MINUTOS = 10;

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final JwtProperties jwtProperties;
    private final AuditoriaService auditoriaService;
    private final TotpService totpService;
    private final CacheManager cacheManager;

    @Override
    @Transactional
    public LoginResponse login(LoginRequest request) {
        String ip = getClientIp();
        String userAgent = getUserAgent();

        Usuario usuario = usuarioRepository.findByUsername(request.usuario())
                .orElseThrow(() -> {
                    auditoriaService.registrarIntentoFallido(null, ip, null, userAgent);
                    return new BusinessException(
                            ErrorCode.INVALID_CREDENTIALS,
                            "Usuario o contraseña incorrectos");
                });

        int intentos = auditoriaService.contarIntentosFallidos(usuario.getId(), VENTANA_MINUTOS);
        if (intentos >= MAX_INTENTOS_FALLIDOS) {
            throw new BusinessException(
                    ErrorCode.LOGIN_BLOCKED,
                    "Demasiados intentos fallidos. Intente nuevamente en unos minutos",
                    Map.of("minutosRestantes", VENTANA_MINUTOS));
        }

        if (!passwordEncoder.matches(request.password(), usuario.getPassword())) {
            auditoriaService.registrarIntentoFallido(usuario.getId(), ip, null, userAgent);
            throw new BusinessException(
                    ErrorCode.INVALID_CREDENTIALS,
                    "Usuario o contraseña incorrectos");
        }

        String rol = usuario.getRol().getNombreRol();

        if (usuario.isDosFactorHabilitado()) {
            Cache cache = cacheManager.getCache(CacheConfig.CACHE_SESION_2FA_PENDIENTE);
            if (cache != null) {
                cache.put(usuario.getId(), true);
            }
            return LoginResponse.requires2fa(usuario.getId(), rol);
        }

        String token = jwtUtil.generateToken(usuario.getId(), usuario.getUsername(), rol, false);
        auditoriaService.registrarLogin(usuario.getId(), ip, null, userAgent);
        return LoginResponse.withToken(token, jwtProperties.expiration(), usuario.getId(), rol);
    }

    @Override
    @Transactional
    public LoginResponse verify2fa(Verify2faRequest request) {
        String ip = getClientIp();
        String userAgent = getUserAgent();

        Cache cache = cacheManager.getCache(CacheConfig.CACHE_SESION_2FA_PENDIENTE);
        Boolean pendiente = cache != null ? cache.get(request.idUsuario(), Boolean.class) : null;

        if (pendiente == null) {
            throw new BusinessException(
                    ErrorCode.TWOFA_SESSION_EXPIRED,
                    "La sesión de verificación expiró. Inicie sesión nuevamente");
        }

        Usuario usuario = usuarioRepository.findById(request.idUsuario())
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.INVALID_CREDENTIALS,
                        "Usuario no encontrado"));

        totpService.verificarCodigo(usuario.getTotpSecret(), request.codigoTotp());

        if (cache != null) {
            cache.evict(request.idUsuario());
        }

        String rol = usuario.getRol().getNombreRol();
        String token = jwtUtil.generateToken(usuario.getId(), usuario.getUsername(), rol, true);
        auditoriaService.registrarLogin(usuario.getId(), ip, null, userAgent);

        return LoginResponse.withToken(token, jwtProperties.expiration(), usuario.getId(), rol);
    }

    private String getClientIp() {
        try {
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder
                    .currentRequestAttributes()).getRequest();
            return request.getRemoteAddr();
        } catch (Exception e) {
            return null;
        }
    }

    private String getUserAgent() {
        try {
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder
                    .currentRequestAttributes()).getRequest();
            return request.getHeader("User-Agent");
        } catch (Exception e) {
            return null;
        }
    }
}
