package com.institucion.sigea.usuario.controller;

import com.institucion.sigea.usuario.dto.request.RolRequest;
import com.institucion.sigea.usuario.dto.response.RolResponse;
import com.institucion.sigea.usuario.service.RolService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/roles")
@RequiredArgsConstructor
public class RolController {

    private final RolService rolService;

    @GetMapping
    @PreAuthorize("hasPermission(null, 'ROLES', 'VER')")
    public List<RolResponse> listar() { return rolService.listar(); }

    @PostMapping
    @PreAuthorize("hasPermission(null, 'ROLES', 'CREAR')")
    @ResponseStatus(HttpStatus.CREATED)
    public RolResponse crear(@Valid @RequestBody RolRequest request) { return rolService.crear(request); }

    @PutMapping("/{id}")
    @PreAuthorize("hasPermission(null, 'ROLES', 'EDITAR')")
    public RolResponse actualizar(@PathVariable Long id, @Valid @RequestBody RolRequest request) {
        return rolService.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasPermission(null, 'ROLES', 'ELIMINAR')")
    public ResponseEntity<Map<String, String>> eliminar(@PathVariable Long id) {
        rolService.eliminar(id);
        return ResponseEntity.ok(Map.of("message", "Rol eliminado exitosamente"));
    }
}
