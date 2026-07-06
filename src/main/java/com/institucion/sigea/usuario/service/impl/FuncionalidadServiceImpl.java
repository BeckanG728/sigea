package com.institucion.sigea.usuario.service.impl;

import com.institucion.sigea.usuario.dto.response.FuncionalidadTreeResponse;
import com.institucion.sigea.usuario.entity.Funcionalidad;
import com.institucion.sigea.usuario.repository.FuncionalidadRepository;
import com.institucion.sigea.usuario.service.FuncionalidadService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FuncionalidadServiceImpl implements FuncionalidadService {

    private final FuncionalidadRepository funcionalidadRepository;

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
