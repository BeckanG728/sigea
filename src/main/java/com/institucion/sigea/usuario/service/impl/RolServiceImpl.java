package com.institucion.sigea.usuario.service.impl;

import com.institucion.sigea.auditoria.Auditable;
import com.institucion.sigea.core.enums.TipoOperacionAuditoria;
import com.institucion.sigea.core.exception.BusinessException;
import com.institucion.sigea.core.exception.ErrorCode;
import com.institucion.sigea.usuario.dto.request.RolRequest;
import com.institucion.sigea.usuario.dto.response.RolResponse;
import com.institucion.sigea.usuario.entity.Rol;
import com.institucion.sigea.usuario.entity.Usuario;
import com.institucion.sigea.usuario.repository.RolRepository;
import com.institucion.sigea.usuario.service.RolService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RolServiceImpl implements RolService {

    private final RolRepository rolRepository;

    @Override
    @Transactional
    @Auditable(modulo = "rol", operacion = TipoOperacionAuditoria.INSERT)
    public RolResponse crear(RolRequest request) {
        if (rolRepository.existsByNombreRol(request.nombreRol())) {
            throw new BusinessException(ErrorCode.ROL_DUPLICADO, "El rol ya existe",
                    Map.of("nombreRol", request.nombreRol()));
        }
        Rol rol = new Rol(request.nombreRol());
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
        rol.setNombreRol(request.nombreRol());
        rolRepository.save(rol);
        return toResponse(rol);
    }

    @Override
    @Transactional
    @Auditable(modulo = "rol", operacion = TipoOperacionAuditoria.DELETE)
    public void eliminar(Long id) {
        Rol rol = rolRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ROL_NO_ENCONTRADO, "Rol no encontrado"));
        if (rol.getUsuarios().stream().anyMatch(Usuario::isEstado)) {
            throw new BusinessException(ErrorCode.ROL_CON_USUARIOS,
                    "No se puede eliminar un rol con usuarios asignados");
        }
        rol.setEstado(false);
        rolRepository.save(rol);
    }

    private RolResponse toResponse(Rol rol) {
        return new RolResponse(rol.getId(), rol.getNombreRol(), rol.isEstado());
    }
}