package com.institucion.sigea.usuario.controller;

import com.institucion.sigea.core.api.PageResponse;
import com.institucion.sigea.core.api.SimpleResponse;
import com.institucion.sigea.usuario.dto.request.ActualizarUsuarioRequest;
import com.institucion.sigea.usuario.dto.request.CrearUsuarioRequest;
import com.institucion.sigea.usuario.dto.response.UsuarioResponse;
import com.institucion.sigea.usuario.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    @PostMapping
    public ResponseEntity<SimpleResponse> crear(@Valid @RequestBody CrearUsuarioRequest request) {
        SimpleResponse response = usuarioService.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SimpleResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody ActualizarUsuarioRequest request) {
        return ResponseEntity.ok(usuarioService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<SimpleResponse> eliminar(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.eliminar(id));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponse> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.obtenerPorId(id));
    }

    @GetMapping
    public ResponseEntity<PageResponse<UsuarioResponse>> listar(Pageable pageable) {
        return ResponseEntity.ok(usuarioService.listar(pageable));
    }
}
