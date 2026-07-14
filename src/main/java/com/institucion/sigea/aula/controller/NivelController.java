package com.institucion.sigea.aula.controller;

import com.institucion.sigea.aula.dto.response.NivelResponse;
import com.institucion.sigea.aula.service.NivelService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/niveles")
@RequiredArgsConstructor
public class NivelController {

    private final NivelService nivelService;

    @GetMapping
    @PreAuthorize("hasPermission(null, 'AULA', 'VER')")
    public List<NivelResponse> listar() {
        return nivelService.listar();
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasPermission(null, 'AULA', 'ELIMINAR')")
    public void eliminar(@PathVariable Long id) {
        nivelService.eliminar(id);
    }
}
