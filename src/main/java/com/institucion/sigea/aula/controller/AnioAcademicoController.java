package com.institucion.sigea.aula.controller;

import com.institucion.sigea.aula.entity.AnioAcademico;
import com.institucion.sigea.aula.service.AnioAcademicoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/anios-academicos")
@RequiredArgsConstructor
public class AnioAcademicoController {

    private final AnioAcademicoService anioAcademicoService;

    @GetMapping
    @PreAuthorize("hasPermission(null, 'AULA', 'VER')")
    public List<AnioAcademico> listar() {
        return anioAcademicoService.listar();
    }

    @GetMapping("/activo")
    @PreAuthorize("hasPermission(null, 'AULA', 'VER')")
    public AnioAcademico obtenerActivo() {
        return anioAcademicoService.obtenerActivo();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasPermission(null, 'AULA', 'CREAR')")
    public AnioAcademico crear(@RequestBody Map<String, Integer> body) {
        return anioAcademicoService.crear(body.get("anio"));
    }

    @PostMapping("/{id}/activar")
    @PreAuthorize("hasPermission(null, 'AULA', 'EDITAR')")
    public void activar(@PathVariable Long id) {
        anioAcademicoService.activar(id);
    }
}
