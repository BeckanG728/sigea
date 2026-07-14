package com.institucion.sigea.matricula.controller;

import com.institucion.sigea.matricula.dto.request.MatriculaRequest;
import com.institucion.sigea.matricula.dto.response.MatriculaResponse;
import com.institucion.sigea.matricula.service.MatriculaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/matriculas") // sin /api: el context-path global ya lo agrega
public class MatriculaController {

    private final MatriculaService matriculaService;

    public MatriculaController(MatriculaService matriculaService) {
        this.matriculaService = matriculaService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasPermission(null, 'MATRICULA_REGISTRAR', 'CREAR')")
    public MatriculaResponse matricular(@Valid @RequestBody MatriculaRequest request) {
        return matriculaService.matricular(request);
    }
}
