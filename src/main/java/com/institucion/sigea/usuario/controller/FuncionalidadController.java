package com.institucion.sigea.usuario.controller;

import com.institucion.sigea.usuario.dto.response.FuncionalidadTreeResponse;
import com.institucion.sigea.usuario.service.FuncionalidadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/funcionalidades")
@RequiredArgsConstructor
public class FuncionalidadController {

    private final FuncionalidadService funcionalidadService;

    @GetMapping("/tree")
    @PreAuthorize("hasRole('SUPERUSUARIO')")
    public ResponseEntity<List<FuncionalidadTreeResponse>> obtenerArbol() {
        return ResponseEntity.ok(funcionalidadService.obtenerArbol());
    }
}
