package com.institucion.sigea.concepto.controller;

import com.institucion.sigea.concepto.dto.request.ConceptoRequest;
import com.institucion.sigea.concepto.dto.response.ConceptoResponse;
import com.institucion.sigea.concepto.service.ConceptoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/conceptos") // sin /api, igual que Alumno y Aula
public class ConceptoController {

    private final ConceptoService conceptoService;

    public ConceptoController(ConceptoService conceptoService) {
        this.conceptoService = conceptoService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ConceptoResponse crear(@Valid @RequestBody ConceptoRequest request) {
        return conceptoService.crear(request);
    }

    @PutMapping("/{id}")
    public ConceptoResponse actualizar(@PathVariable Long id, @Valid @RequestBody ConceptoRequest request) {
        return conceptoService.actualizar(id, request);
    }
}