package com.institucion.sigea.usuario.service.impl;

import com.institucion.sigea.usuario.dto.response.FuncionalidadTreeResponse;
import com.institucion.sigea.usuario.dto.response.MisPermisosResponse;
import com.institucion.sigea.usuario.dto.response.PermisoInfo;
import com.institucion.sigea.usuario.entity.Funcionalidad;
import com.institucion.sigea.usuario.repository.FuncionalidadRepository;
import com.institucion.sigea.usuario.service.FuncionalidadService;
import com.institucion.sigea.usuario.service.PermisoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FuncionalidadServiceImpl implements FuncionalidadService {

    private final FuncionalidadRepository funcionalidadRepository;
    private final PermisoService permisoService;

    @Override
    @Transactional(readOnly = true)
    public List<FuncionalidadTreeResponse> obtenerArbol() {
        List<Funcionalidad> todas = funcionalidadRepository.findAll();

        Map<Long, List<Funcionalidad>> hijosPorPadre = todas.stream()
                .filter(f -> f.getPadre() != null && f.isEstado())
                .collect(Collectors.groupingBy(f -> f.getPadre().getId()));

        return todas.stream()
                .filter(f -> f.getPadre() == null && f.isEstado())
                .map(f -> toTree(f, hijosPorPadre))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MisPermisosResponse> obtenerMisPermisos(Long idRol) {
        List<Funcionalidad> todas = funcionalidadRepository.findAll();
        List<PermisoInfo> permisos = permisoService.obtenerPermisos(idRol); // ya existe de P1-08

        Map<Long, List<Funcionalidad>> hijosPorPadre = todas.stream()
                .filter(f -> f.getPadre() != null && f.isEstado())
                .collect(Collectors.groupingBy(f -> f.getPadre().getId()));

        return todas.stream()
                .filter(f -> f.getPadre() == null && f.isEstado())
                .map(f -> toMisPermisos(f, hijosPorPadre, permisos))
                .filter(Objects::nonNull) // se descartan las raíces donde el usuario no tiene "ver" en ningún hijo
                .toList();
    }

    private MisPermisosResponse toMisPermisos(Funcionalidad f, Map<Long, List<Funcionalidad>> hijosPorPadre,
                                              List<PermisoInfo> permisos) {
        PermisoInfo permiso = permisos.stream()
                .filter(p -> p.nombreFuncionalidad().equalsIgnoreCase(f.getNombre()))
                .findFirst().orElse(null);

        List<MisPermisosResponse> hijos = hijosPorPadre.getOrDefault(f.getId(), List.of()).stream()
                .map(h -> toMisPermisos(h, hijosPorPadre, permisos))
                .filter(Objects::nonNull)
                .toList();

        boolean tienePermisoPropio = permiso != null && permiso.ver();
        if (!tienePermisoPropio && hijos.isEmpty()) {
            return null; // ni el nodo ni ningún hijo tiene "ver" -> se oculta del árbol
        }

        MisPermisosResponse.PermisosFlags flags = permiso != null
                ? new MisPermisosResponse.PermisosFlags(permiso.ver(), permiso.crear(), permiso.editar(), permiso.eliminar(), permiso.imprimir())
                : new MisPermisosResponse.PermisosFlags(false, false, false, false, false);

        return new MisPermisosResponse(f.getId(), f.getNombre(), flags, hijos);
    }

    private FuncionalidadTreeResponse toTree(Funcionalidad f,
                                              Map<Long, List<Funcionalidad>> hijosPorPadre) {
        List<FuncionalidadTreeResponse> children = hijosPorPadre
                .getOrDefault(f.getId(), List.of())
                .stream()
                .map(h -> toTree(h, hijosPorPadre))
                .toList();

        return new FuncionalidadTreeResponse(
                f.getId(),
                f.getNombre(),
                f.getIcono(),
                children
        );
    }
}
