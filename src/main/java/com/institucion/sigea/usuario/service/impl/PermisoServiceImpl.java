package com.institucion.sigea.usuario.service.impl;

import com.institucion.sigea.config.CacheConfig;
import com.institucion.sigea.core.exception.BusinessException;
import com.institucion.sigea.core.exception.ErrorCode;
import com.institucion.sigea.usuario.dto.request.PermisoItem;
import com.institucion.sigea.usuario.dto.response.PermisoInfo;
import com.institucion.sigea.usuario.entity.Funcionalidad;
import com.institucion.sigea.usuario.entity.Rol;
import com.institucion.sigea.usuario.entity.RolFuncionalidad;
import com.institucion.sigea.usuario.repository.FuncionalidadRepository;
import com.institucion.sigea.usuario.repository.RolFuncionalidadRepository;
import com.institucion.sigea.usuario.repository.RolRepository;
import com.institucion.sigea.usuario.service.PermisoService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PermisoServiceImpl implements PermisoService {

    private final RolRepository rolRepository;
    private final FuncionalidadRepository funcionalidadRepository;
    private final RolFuncionalidadRepository rolFuncionalidadRepository;

    @Override
    @Transactional
    @CacheEvict(value = CacheConfig.CACHE_PERMISOS_POR_ROL, key = "#idRol")
    public void aplicar(Long idRol, List<PermisoItem> items) {
        Rol rol = rolRepository.findById(idRol)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.ROL_NO_ENCONTRADO,
                        "Rol no encontrado: " + idRol,
                        Map.of("idRol", idRol)));

        List<RolFuncionalidad> existentes = rolFuncionalidadRepository.findByRolId(idRol);
        Map<Long, RolFuncionalidad> mapaExistentes = existentes.stream()
                .collect(Collectors.toMap(
                        rf -> rf.getFuncionalidad().getId(),
                        Function.identity()));

        Set<Long> idsDelRequest = items.stream()
                .map(PermisoItem::idFuncionalidad)
                .collect(Collectors.toSet());

        List<RolFuncionalidad> paraGuardar = new ArrayList<>();

        for (PermisoItem item : items) {
            RolFuncionalidad rf = mapaExistentes.get(item.idFuncionalidad());
            if (rf != null) {
                rf.setVer(item.ver());
                rf.setCrear(item.crear());
                rf.setEditar(item.editar());
                rf.setEliminar(item.eliminar());
                rf.setImprimir(item.imprimir());
                rf.setEstado(true);
                paraGuardar.add(rf);
            } else {
                Funcionalidad funcionalidad = funcionalidadRepository
                        .getReferenceById(item.idFuncionalidad());
                rf = new RolFuncionalidad(rol, funcionalidad,
                        item.ver(), item.crear(), item.editar(),
                        item.eliminar(), item.imprimir());
                paraGuardar.add(rf);
            }
        }

        for (RolFuncionalidad rf : existentes) {
            Long idFunc = rf.getFuncionalidad().getId();
            if (!idsDelRequest.contains(idFunc)) {
                rf.setEstado(false);
                paraGuardar.add(rf);
            }
        }

        rolFuncionalidadRepository.saveAll(paraGuardar);
    }

    @Override
    @Cacheable(value = CacheConfig.CACHE_PERMISOS_POR_ROL, key = "#idRol")
    public List<PermisoInfo> obtenerPermisos(Long idRol) {
        return rolFuncionalidadRepository.findByRolIdAndEstadoTrue(idRol)
                .stream()
                .map(rf -> new PermisoInfo(
                        rf.getFuncionalidad().getId(),
                        rf.getFuncionalidad().getNombre(),
                        rf.isVer(),
                        rf.isCrear(),
                        rf.isEditar(),
                        rf.isEliminar(),
                        rf.isImprimir()))
                .toList();
    }
}
