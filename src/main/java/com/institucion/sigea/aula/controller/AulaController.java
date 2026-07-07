package com.institucion.sigea.aula.controller;

import com.institucion.sigea.aula.dto.request.AulaRequest;
import com.institucion.sigea.aula.dto.response.AulaResponse;
import com.institucion.sigea.aula.service.AulaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/aulas")
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
}