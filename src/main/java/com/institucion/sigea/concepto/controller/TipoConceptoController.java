package com.institucion.sigea.concepto.controller;

import com.institucion.sigea.concepto.dto.request.TipoConceptoRequest;
import com.institucion.sigea.concepto.dto.response.TipoConceptoResponse;
import com.institucion.sigea.concepto.service.TipoConceptoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tipos-concepto") // sin /api
public class TipoConceptoController {

    private final TipoConceptoService tipoConceptoService;

    public TipoConceptoController(TipoConceptoService tipoConceptoService) {
        this.tipoConceptoService = tipoConceptoService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TipoConceptoResponse crear(@Valid @RequestBody TipoConceptoRequest request) {
        return tipoConceptoService.crear(request);
    }

    @GetMapping
    public List<TipoConceptoResponse> listar() {
        return tipoConceptoService.listar();
    }

    @PutMapping("/{id}")
    public TipoConceptoResponse actualizar(@PathVariable Long id, @Valid @RequestBody TipoConceptoRequest request) {
        return tipoConceptoService.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        tipoConceptoService.eliminar(id);
    }
}