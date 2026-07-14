package com.institucion.sigea.alumno.controller;

import com.institucion.sigea.alumno.dto.request.AlumnoRequest;
import com.institucion.sigea.alumno.dto.response.AlumnoBusquedaResponse;
import com.institucion.sigea.alumno.dto.response.AlumnoResponse;
import com.institucion.sigea.alumno.service.AlumnoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/alumnos")
@RequiredArgsConstructor
public class AlumnoController {

    private final AlumnoService alumnoService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasPermission(null, 'ALUMNO', 'CREAR')")
    public AlumnoResponse crear(@Valid @RequestBody AlumnoRequest request) {
        return alumnoService.crear(request);
    }

    @GetMapping
    @PreAuthorize("hasPermission(null, 'ALUMNO', 'VER')")
    public List<AlumnoBusquedaResponse> buscar(@RequestParam(required = false) String nombres) {
        return alumnoService.buscar(nombres);
    }

    @GetMapping("/documento/{numero}")
    @PreAuthorize("hasPermission(null, 'ALUMNO', 'VER')")
    public List<AlumnoBusquedaResponse> buscarPorDocumento(@PathVariable String numero) {
        return alumnoService.buscarPorDocumento(numero);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasPermission(null, 'ALUMNO', 'ELIMINAR')")
    public void eliminar(@PathVariable Long id) {
        alumnoService.eliminar(id);
    }
}