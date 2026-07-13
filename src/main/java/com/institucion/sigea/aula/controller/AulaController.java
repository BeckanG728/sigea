package com.institucion.sigea.aula.controller;

import com.institucion.sigea.aula.dto.request.AulaRequest;
import com.institucion.sigea.aula.dto.response.AlumnoAulaResponse;
import com.institucion.sigea.aula.dto.response.AulaBusquedaResponse;
import com.institucion.sigea.aula.dto.response.AulaListadoResponse;
import com.institucion.sigea.aula.dto.response.AulaResponse;
import com.institucion.sigea.aula.service.AulaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/aulas")
@RequiredArgsConstructor
public class AulaController {

    private final AulaService aulaService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasPermission(null, 'AULA', 'CREAR')")
    public AulaResponse crear(@Valid @RequestBody AulaRequest request) {
        return aulaService.crear(request);
    }

    @GetMapping
    @PreAuthorize("hasPermission(null, 'AULA', 'VER')")
    public List<AulaListadoResponse> listar(
            @RequestParam(required = false) Long anioAcademico,
            @RequestParam(required = false) Long nivel) {
        return aulaService.listarConDetalle(anioAcademico, nivel);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasPermission(null, 'AULA', 'VER')")
    public AulaListadoResponse obtener(@PathVariable Long id) {
        return aulaService.obtenerPorId(id);
    }

    @GetMapping("/{id}/alumnos")
    @PreAuthorize("hasPermission(null, 'AULA', 'VER')")
    public List<AlumnoAulaResponse> listarAlumnos(
            @PathVariable Long id,
            @RequestParam(required = false) Long anioAcademico) {
        return aulaService.obtenerAlumnos(id, anioAcademico);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasPermission(null, 'AULA', 'EDITAR')")
    public AulaResponse actualizar(@PathVariable Long id, @Valid @RequestBody AulaRequest request) {
        return aulaService.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasPermission(null, 'AULA', 'ELIMINAR')")
    public void eliminar(@PathVariable Long id) {
        aulaService.eliminar(id);
    }
}