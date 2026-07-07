package com.institucion.sigea.aula.controller;

import com.institucion.sigea.aula.dto.request.AulaRequest;
import com.institucion.sigea.aula.dto.response.AulaBusquedaResponse;
import com.institucion.sigea.aula.dto.response.AulaResponse;
import com.institucion.sigea.aula.service.AulaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/aulas") // sin /api: el context-path global ya lo agrega
public class AulaController {

    private final AulaService aulaService;

    public AulaController(AulaService aulaService) {
        this.aulaService = aulaService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AulaResponse crear(@Valid @RequestBody AulaRequest request) {
        return aulaService.crear(request);
    }

    @GetMapping
    public List<AulaBusquedaResponse> buscar(
            @RequestParam(required = false) Long anioAcademico,
            @RequestParam(required = false) Long nivel) {
        return aulaService.buscar(anioAcademico, nivel);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        aulaService.eliminar(id);
    }
}