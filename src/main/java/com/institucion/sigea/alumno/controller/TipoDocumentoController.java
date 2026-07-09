package com.institucion.sigea.alumno.controller;

import com.institucion.sigea.alumno.entity.TipoDocumento;
import com.institucion.sigea.alumno.service.TipoDocumentoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tipos-documento")
@RequiredArgsConstructor
public class TipoDocumentoController {

    private final TipoDocumentoService tipoDocumentoService;

    @GetMapping
    public List<TipoDocumento> listar() {
        return tipoDocumentoService.listar();
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        tipoDocumentoService.eliminar(id);
    }
}
