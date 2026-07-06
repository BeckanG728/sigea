package com.institucion.sigea.security.permission;

import com.institucion.sigea.core.exception.BusinessException;
import com.institucion.sigea.core.exception.ErrorCode;
import com.institucion.sigea.security.jwt.JwtPrincipal;
import com.institucion.sigea.usuario.dto.response.PermisoInfo;
import com.institucion.sigea.usuario.entity.Rol;
import com.institucion.sigea.usuario.repository.RolRepository;
import com.institucion.sigea.usuario.service.PermisoService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.List;

@Component
@RequiredArgsConstructor
public class PermisoEvaluator implements PermissionEvaluator {

    private static final String ROL_SUPERUSUARIO = "SUPERUSUARIO";

    private final PermisoService permisoService;
    private final RolRepository rolRepository;

    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject,
                                 Object permission) {
        return false;
    }

    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId,
                                 String targetType, Object permission) {
        if (authentication == null || !(authentication.getPrincipal() instanceof JwtPrincipal principal)) {
            throw new BusinessException(ErrorCode.PERMISO_DENEGADO, "No autenticado");
        }

        if (ROL_SUPERUSUARIO.equals(principal.rol())) {
            return true;
        }

        Long idRol = rolRepository.findByNombreRol(principal.rol())
                .map(Rol::getId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.PERMISO_DENEGADO, "Rol no encontrado: " + principal.rol()));

        List<PermisoInfo> permisos = permisoService.obtenerPermisos(idRol);

        String funcionalidad = targetType;
        String accion = permission.toString().toUpperCase();

        return permisos.stream()
                .filter(p -> p.nombreFuncionalidad().equalsIgnoreCase(funcionalidad))
                .findFirst()
                .map(p -> switch (accion) {
                    case "VER" -> p.ver();
                    case "CREAR" -> p.crear();
                    case "EDITAR" -> p.editar();
                    case "ELIMINAR" -> p.eliminar();
                    case "IMPRIMIR" -> p.imprimir();
                    default -> false;
                })
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.PERMISO_DENEGADO,
                        "No tiene permiso para " + accion + " en " + funcionalidad));
    }
}
