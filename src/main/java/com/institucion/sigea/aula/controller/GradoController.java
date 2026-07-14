package com.institucion.sigea.aula.controller;

import com.institucion.sigea.aula.dto.response.GradoResponse;
import com.institucion.sigea.aula.entity.Grado;
import com.institucion.sigea.aula.service.GradoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/grados")
@RequiredArgsConstructor
public class GradoController {

    private final GradoService gradoService;

    @GetMapping
    @PreAuthorize("hasPermission(null, 'AULAS', 'VER')")
    public List<GradoResponse> listar() {
        return gradoService.listar().stream()
                .map(g -> new GradoResponse(g.getId(), g.getNombreGrado(), g.getNivel().getId()))
                .toList();
    }
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasPermission(null, 'AULAS', 'ELIMINAR')")
    public void eliminar(@PathVariable Long id) {
        gradoService.eliminar(id);
    }
}
