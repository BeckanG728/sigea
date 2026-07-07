package com.institucion.sigea.concepto.controller;

import com.institucion.sigea.concepto.dto.request.ClonadoRequest;
import com.institucion.sigea.concepto.dto.request.ConceptoRequest;
import com.institucion.sigea.concepto.dto.response.ClonadoResponse;
import com.institucion.sigea.concepto.dto.response.ConceptoResponse;
import com.institucion.sigea.concepto.service.ConceptoService;
import com.institucion.sigea.concepto.service.ClonadorConceptoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/conceptos") // sin /api, igual que Alumno y Aula
public class ConceptoController {

    private final ConceptoService conceptoService;
    private final ClonadorConceptoService clonadorConceptoService;

    public ConceptoController(ConceptoService conceptoService, ClonadorConceptoService clonadorConceptoService) {
        this.conceptoService = conceptoService;
        this.clonadorConceptoService = clonadorConceptoService;
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

    @PostMapping("/clonar")
    public ClonadoResponse clonar(@Valid @RequestBody ClonadoRequest request) {
        return clonadorConceptoService.clonar(request.anioOrigen(), request.anioDestino());
    }
}