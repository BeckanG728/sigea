package com.institucion.sigea.usuario.controller;

import com.institucion.sigea.usuario.dto.request.PermisoRequest;
import com.institucion.sigea.usuario.dto.response.PermisoResponse;
import com.institucion.sigea.usuario.service.PermisoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("roles")
public class PermisoController {

    private final PermisoService permisoService;

    public PermisoController(PermisoService permisoService) {
        this.permisoService = permisoService;
    }

    @PutMapping("/{idRol}/permisos")
    public ResponseEntity<PermisoResponse> aplicarPermisos(
            @PathVariable Long idRol,
            @Valid @RequestBody PermisoRequest request) {
        permisoService.aplicar(idRol, request.permisos());
        return ResponseEntity.ok(new PermisoResponse(idRol, "Permisos actualizados correctamente"));
    }
}
