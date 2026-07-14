package com.institucion.sigea.concepto.controller;

import com.institucion.sigea.concepto.dto.request.ClonadoRequest;
import com.institucion.sigea.concepto.dto.request.ConceptoRequest;
import com.institucion.sigea.concepto.dto.response.ClonadoResponse;
import com.institucion.sigea.concepto.dto.response.ConceptoResponse;
import com.institucion.sigea.concepto.service.ClonadorConceptoService;
import com.institucion.sigea.concepto.service.ConceptoService;
import com.institucion.sigea.core.api.PageResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    @PreAuthorize("hasPermission(null, 'CONCEPTOS', 'CREAR')")
    public ConceptoResponse crear(@Valid @RequestBody ConceptoRequest request) {
        return conceptoService.crear(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasPermission(null, 'CONCEPTOS', 'EDITAR')")
    public ConceptoResponse actualizar(@PathVariable Long id, @Valid @RequestBody ConceptoRequest request) {
        return conceptoService.actualizar(id, request);
    }

    @PostMapping("/clonar")
    @PreAuthorize("hasPermission(null, 'CONCEPTOS', 'CREAR')")
    public ClonadoResponse clonar(@Valid @RequestBody ClonadoRequest request) {
        return clonadorConceptoService.clonar(request.anioOrigen(), request.anioDestino());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasPermission(null, 'CONCEPTOS', 'ELIMINAR')")
    public void eliminar(@PathVariable Long id) {
        conceptoService.eliminar(id);
    }

    @GetMapping
    @PreAuthorize("hasPermission(null, 'CONCEPTOS', 'VER')")
    public PageResponse<ConceptoResponse> listar(
            @RequestParam(required = false) Long anioAcademicoId,
            @PageableDefault(size = 6, sort = "ordenPago") Pageable pageable) {
        return conceptoService.listar(anioAcademicoId, pageable);
    }

    @GetMapping(params = "anio")
    @PreAuthorize("hasPermission(null, 'CONCEPTOS', 'VER')")
    public List<ConceptoResponse> listarPorAnio(@RequestParam Integer anio) {
        return conceptoService.listarPorAnio(anio);
    }
}