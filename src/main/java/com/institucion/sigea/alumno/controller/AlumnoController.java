package com.institucion.sigea.alumno.controller;

import com.institucion.sigea.alumno.dto.request.AlumnoRequest;
import com.institucion.sigea.alumno.dto.response.AlumnoBusquedaResponse;
import com.institucion.sigea.alumno.dto.response.AlumnoResponse;
import com.institucion.sigea.alumno.service.AlumnoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/alumnos") // sin /api
public class AlumnoController {

    private final AlumnoService alumnoService;

    public AlumnoController(AlumnoService alumnoService) {
        this.alumnoService = alumnoService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AlumnoResponse crear(@Valid @RequestBody AlumnoRequest request) {
        return alumnoService.crear(request);
    }

    @GetMapping
    public List<AlumnoBusquedaResponse> buscar(@RequestParam(required = false) String nombres) {
        return alumnoService.buscar(nombres);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        alumnoService.eliminar(id);
    }
}