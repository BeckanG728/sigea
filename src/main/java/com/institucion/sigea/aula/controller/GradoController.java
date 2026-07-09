package com.institucion.sigea.aula.controller;

import com.institucion.sigea.aula.entity.Grado;
import com.institucion.sigea.aula.service.GradoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/grados")
@RequiredArgsConstructor
public class GradoController {

    private final GradoService gradoService;

    @GetMapping
    public List<Grado> listar() {
        return gradoService.listar();
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        gradoService.eliminar(id);
    }
}
