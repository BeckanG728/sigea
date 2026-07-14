package com.institucion.sigea.alumno.controller;

import com.institucion.sigea.alumno.dto.response.TipoDocumentoResponse;
import com.institucion.sigea.alumno.service.TipoDocumentoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tipos-documento")
@RequiredArgsConstructor
public class TipoDocumentoController {

    private final TipoDocumentoService tipoDocumentoService;

    @GetMapping
    @PreAuthorize("hasPermission(null, 'ALUMNO', 'CREAR')")
    public List<TipoDocumentoResponse> listar() {
        return tipoDocumentoService.listar();
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasPermission(null, 'ALUMNO', 'ELIMINAR')")
    public void eliminar(@PathVariable Long id) {
        tipoDocumentoService.eliminar(id);
    }
}
