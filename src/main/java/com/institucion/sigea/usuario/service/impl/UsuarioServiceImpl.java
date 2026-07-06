package com.institucion.sigea.usuario.service.impl;

import com.institucion.sigea.auditoria.Auditable;
import com.institucion.sigea.config.CacheConfig;
import com.institucion.sigea.core.api.PageResponse;
import com.institucion.sigea.core.enums.TipoOperacionAuditoria;
import com.institucion.sigea.core.exception.BusinessException;
import com.institucion.sigea.core.exception.ErrorCode;
import com.institucion.sigea.security.jwt.JwtPrincipal;
import com.institucion.sigea.usuario.dto.request.ActualizarUsuarioRequest;
import com.institucion.sigea.usuario.dto.request.CrearUsuarioRequest;
import com.institucion.sigea.usuario.dto.response.UsuarioResponse;
import com.institucion.sigea.usuario.entity.Rol;
import com.institucion.sigea.usuario.entity.Usuario;
import com.institucion.sigea.usuario.mapper.UsuarioMapper;
import com.institucion.sigea.usuario.repository.RolRepository;
import com.institucion.sigea.usuario.repository.UsuarioRepository;
import com.institucion.sigea.usuario.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements UsuarioService {

    private static final String ROL_SUPERUSUARIO = "SUPERUSUARIO";

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;
    private final CacheManager cacheManager;
    private final UsuarioMapper usuarioMapper;

    @Override
    @Transactional
    @Auditable(modulo = "usuario", operacion = TipoOperacionAuditoria.INSERT)
    public UsuarioResponse crear(CrearUsuarioRequest request) {
        Rol rol = rolRepository.findById(request.idRol())
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.ROL_NO_ENCONTRADO,
                        "Rol no encontrado: " + request.idRol(),
                        Map.of("idRol", request.idRol())));

        if (usuarioRepository.existsByUsername(request.usuario())) {
            throw new BusinessException(
                    ErrorCode.VALIDACION_FORMULARIO,
                    "El nombre de usuario ya existe: " + request.usuario(),
                    Map.of("usuario", request.usuario()));
        }

        Usuario usuario = new Usuario();
        usuario.setUsername(request.usuario());
        usuario.setPassword(passwordEncoder.encode(request.password()));
        usuario.setRol(rol);
        usuario.setDosFactorHabilitado(false);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof JwtPrincipal principal) {
            usuario.setUsuarioCreacion(
                    usuarioRepository.getReferenceById(principal.userId()));
        }

        usuario = usuarioRepository.save(usuario);
        return usuarioMapper.toResponse(usuario);
    }

    @Override
    @Transactional
    @Auditable(modulo = "usuario", operacion = TipoOperacionAuditoria.UPDATE)
    public UsuarioResponse actualizar(Long id, ActualizarUsuarioRequest request) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.VALIDACION_FORMULARIO,
                        "Usuario no encontrado: " + id,
                        Map.of("id", id)));

        if (StringUtils.hasText(request.password())) {
            usuario.setPassword(passwordEncoder.encode(request.password()));
        }

        if (request.idRol() != null) {
            Rol rol = rolRepository.findById(request.idRol())
                    .orElseThrow(() -> new BusinessException(
                            ErrorCode.ROL_NO_ENCONTRADO,
                            "Rol no encontrado: " + request.idRol(),
                            Map.of("idRol", request.idRol())));
            usuario.setRol(rol);
        }

        if (request.estado() != null) {
            usuario.setEstado(request.estado());
        }

        usuario = usuarioRepository.save(usuario);
        return usuarioMapper.toResponse(usuario);
    }

    @Override
    @Transactional
    @Auditable(modulo = "usuario", operacion = TipoOperacionAuditoria.DELETE)
    public void eliminar(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.VALIDACION_FORMULARIO,
                        "Usuario no encontrado: " + id,
                        Map.of("id", id)));

        if (ROL_SUPERUSUARIO.equals(usuario.getRol().getNombreRol())) {
            throw new BusinessException(
                    ErrorCode.USUARIO_NO_ELIMINABLE,
                    "No se puede eliminar al usuario Superusuario");
        }

        usuario.setEstado(false);
        usuarioRepository.save(usuario);

        Cache cache = cacheManager.getCache(CacheConfig.CACHE_USUARIOS_DESACTIVADOS);
        if (cache != null) {
            cache.put(usuario.getId(), usuario.getId());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioResponse obtenerPorId(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.VALIDACION_FORMULARIO,
                        "Usuario no encontrado: " + id,
                        Map.of("id", id)));
        return usuarioMapper.toResponse(usuario);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<UsuarioResponse> listar(Pageable pageable) {
        Page<Usuario> page = usuarioRepository.findAll(pageable);
        return PageResponse.of(page.map(usuarioMapper::toResponse));
    }
}
