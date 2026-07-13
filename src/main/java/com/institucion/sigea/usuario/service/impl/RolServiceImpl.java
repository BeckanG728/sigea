package com.institucion.sigea.usuario.service.impl;

import com.institucion.sigea.auditoria.Auditable;
import com.institucion.sigea.core.enums.TipoOperacionAuditoria;
import com.institucion.sigea.core.exception.BusinessException;
import com.institucion.sigea.core.exception.ErrorCode;
import com.institucion.sigea.usuario.dto.request.RolRequest;
import com.institucion.sigea.usuario.dto.response.RolResponse;
import com.institucion.sigea.usuario.entity.Rol;
import com.institucion.sigea.usuario.repository.RolRepository;
import com.institucion.sigea.usuario.repository.UsuarioRepository;
import com.institucion.sigea.usuario.service.RolService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RolServiceImpl implements RolService {

    private static final String SUPERUSUARIO = "SUPERUSUARIO";

    private final RolRepository rolRepository;
    private final UsuarioRepository usuarioRepository;

    @Override
    @Transactional
    @Auditable(modulo = "rol", operacion = TipoOperacionAuditoria.INSERT)
    public RolResponse crear(RolRequest request) {
        if (rolRepository.existsByNombreIgnoreCase(request.nombre())) {
            throw new BusinessException(ErrorCode.ROL_DUPLICADO, "El rol ya existe",
                    Map.of("nombre", request.nombre()));
        }
        Rol rol = new Rol(request.nombre().toUpperCase());
        rolRepository.save(rol);
        return toResponse(rol);
    }

    @Override
    public List<RolResponse> listar() {
        return rolRepository.findAll().stream().filter(Rol::isEstado).map(this::toResponse).toList();
    }

    @Override
    @Transactional
    @Auditable(modulo = "rol", operacion = TipoOperacionAuditoria.UPDATE)
    public RolResponse actualizar(Long id, RolRequest request) {
        Rol rol = rolRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ROL_NO_ENCONTRADO, "Rol no encontrado"));
        rol.setNombre(request.nombre().toUpperCase());
        rolRepository.save(rol);
        return toResponse(rol);
    }

    @Override
    @Transactional
    @Auditable(modulo = "rol", operacion = TipoOperacionAuditoria.DELETE)
    public void eliminar(Long id) {
        Rol rol = rolRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ROL_NO_ENCONTRADO, "Rol no encontrado"));

        if (SUPERUSUARIO.equals(rol.getNombre())) {
            throw new BusinessException(ErrorCode.ROL_SUPERUSUARIO_NO_ELIMINABLE,
                    "El rol SUPERUSUARIO no puede eliminarse");
        }

        long usuariosActivos = usuarioRepository.countByRolIdAndEstadoTrue(id);
        if (usuariosActivos > 0) {
            throw new BusinessException(ErrorCode.ROL_CON_USUARIOS,
                    "El rol tiene " + usuariosActivos + " usuario(s) asignado(s)",
                    Map.of("cantidadUsuarios", usuariosActivos));
        }

        rol.setEstado(false);
        rolRepository.save(rol);
    }

    private RolResponse toResponse(Rol rol) {
        return new RolResponse(rol.getId(), rol.getNombre(), rol.isEstado());
    }
}