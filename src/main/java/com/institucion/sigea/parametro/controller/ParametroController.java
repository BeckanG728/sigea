package com.institucion.sigea.parametro.controller;

import com.institucion.sigea.parametro.dto.request.ParametroRequest;
import com.institucion.sigea.parametro.service.ParametroService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("parametros")
public class ParametroController {

    private final ParametroService parametroService;

    public ParametroController(ParametroService parametroService) {
        this.parametroService = parametroService;
    }

    @GetMapping("/{clave}")
    public String obtener(@PathVariable String clave) {
        return parametroService.obtener(clave);
    }

    @PutMapping("/{clave}")
    public String actualizar(@PathVariable String clave, @Valid @RequestBody ParametroRequest request) {
        return parametroService.actualizar(clave, request.valor());
    }

    @DeleteMapping("/{clave}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable String clave) {
        parametroService.eliminar(clave);
    }
}