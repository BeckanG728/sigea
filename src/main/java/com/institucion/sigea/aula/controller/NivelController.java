package com.institucion.sigea.aula.controller;

import com.institucion.sigea.aula.entity.Nivel;
import com.institucion.sigea.aula.service.NivelService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/niveles")
@RequiredArgsConstructor
public class NivelController {

    private final NivelService nivelService;

    @GetMapping
    public List<Nivel> listar() {
        return nivelService.listar();
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        nivelService.eliminar(id);
    }
}
