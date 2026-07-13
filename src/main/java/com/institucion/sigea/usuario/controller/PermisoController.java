package com.institucion.sigea.usuario.controller;

import com.institucion.sigea.usuario.dto.request.PermisoRequest;
import com.institucion.sigea.usuario.dto.response.PermisoItemResponse;
import com.institucion.sigea.usuario.dto.response.PermisoResponse;
import com.institucion.sigea.usuario.service.PermisoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/roles")
@RequiredArgsConstructor
public class PermisoController {

    private final PermisoService permisoService;

    @GetMapping("/{idRol}/permisos")
    @PreAuthorize("hasPermission(null, 'ROLES', 'VER')")
    public ResponseEntity<List<PermisoItemResponse>> obtenerPermisos(@PathVariable Long idRol) {
        return ResponseEntity.ok(permisoService.obtenerPermisosConCodigo(idRol));
    }

    @PutMapping("/{idRol}/permisos")
    @PreAuthorize("hasPermission(null, 'ROLES', 'EDITAR')")
    public ResponseEntity<PermisoResponse> aplicarPermisos(
            @PathVariable Long idRol,
            @Valid @RequestBody PermisoRequest request) {
        permisoService.aplicar(idRol, request.permisos());
        return ResponseEntity.ok(new PermisoResponse(idRol, "Permisos actualizados correctamente"));
    }
}
