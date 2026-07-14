package com.institucion.sigea.aula.controller;

import com.institucion.sigea.aula.dto.response.AulaBusquedaResponse;
import com.institucion.sigea.aula.service.AulaService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/aulas/busqueda")
public class AulaBusquedaController {

    private final AulaService aulaService;

    public AulaBusquedaController(AulaService aulaService) {
        this.aulaService = aulaService;
    }

    @GetMapping
    @PreAuthorize("hasPermission(null, 'AULA', 'VER')")
    public List<AulaBusquedaResponse> buscar(
            @RequestParam(required = false) Long anioAcademico,
            @RequestParam(required = false) Long nivel) {
        return aulaService.buscar(anioAcademico, nivel);
    }
}