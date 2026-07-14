package com.institucion.sigea.concepto.controller;

import com.institucion.sigea.concepto.dto.request.ClonadoRequest;
import com.institucion.sigea.concepto.dto.request.ConceptoRequest;
import com.institucion.sigea.concepto.dto.response.ClonadoResponse;
import com.institucion.sigea.concepto.dto.response.ConceptoResponse;
import com.institucion.sigea.concepto.service.ConceptoService;
import com.institucion.sigea.concepto.service.ClonadorConceptoService;
import com.institucion.sigea.core.api.PageResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/conceptos")
public class ConceptoController {

    private final ConceptoService conceptoService;
    private final ClonadorConceptoService clonadorConceptoService;

    public ConceptoController(ConceptoService conceptoService, ClonadorConceptoService clonadorConceptoService) {
        this.conceptoService = conceptoService;
        this.clonadorConceptoService = clonadorConceptoService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasPermission(null, 'CONCEPTO', 'CREAR')")
    public ConceptoResponse crear(@Valid @RequestBody ConceptoRequest request) {
        return conceptoService.crear(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasPermission(null, 'CONCEPTO', 'EDITAR')")
    public ConceptoResponse actualizar(@PathVariable Long id, @Valid @RequestBody ConceptoRequest request) {
        return conceptoService.actualizar(id, request);
    }

    @PostMapping("/clonar")
    @PreAuthorize("hasPermission(null, 'CONCEPTO', 'CREAR')")
    public ClonadoResponse clonar(@Valid @RequestBody ClonadoRequest request) {
        return clonadorConceptoService.clonar(request.anioOrigen(), request.anioDestino());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasPermission(null, 'CONCEPTO', 'ELIMINAR')")
    public void eliminar(@PathVariable Long id) {
        conceptoService.eliminar(id);
    }

    @GetMapping
    @PreAuthorize("hasPermission(null, 'CONCEPTO', 'VER')")
    public PageResponse<ConceptoResponse> listar(
            @RequestParam(required = false) Long anioAcademicoId,
            @PageableDefault(size = 6, sort = "ordenPago") Pageable pageable) {
        return conceptoService.listar(anioAcademicoId, pageable);
    }
}