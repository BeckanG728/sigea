package com.institucion.sigea.alumno.controller;

import com.institucion.sigea.alumno.dto.request.AlumnoRequest;
import com.institucion.sigea.alumno.dto.response.AlumnoBusquedaResponse;
import com.institucion.sigea.alumno.dto.response.AlumnoMatriculaResponse;
import com.institucion.sigea.alumno.dto.response.AlumnoResponse;
import com.institucion.sigea.alumno.service.AlumnoService;
import com.institucion.sigea.pago.dto.response.DeudaMatriculaResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/alumnos")
@RequiredArgsConstructor
@Validated
public class AlumnoController {

    private final AlumnoService alumnoService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasPermission(null, 'ALUMNOS', 'CREAR')")
    public AlumnoResponse crear(@Valid @RequestBody AlumnoRequest request) {
        return alumnoService.crear(request);
    }

    @GetMapping
    @PreAuthorize("hasPermission(null, 'ALUMNOS', 'VER')")
    public List<AlumnoBusquedaResponse> buscar(@RequestParam(required = false) String nombres) {
        return alumnoService.buscar(nombres);
    }

    @GetMapping(params = "q")
    @PreAuthorize("hasPermission(null, 'ALUMNOS', 'VER')")
    public List<AlumnoMatriculaResponse> buscarParaMatricula(@RequestParam String q) {
        return alumnoService.buscarParaMatricula(q);
    }

    @GetMapping("/{id}/deudas")
    @PreAuthorize("hasPermission(null, 'DEUDA_HISTORIAL', 'VER')")
    public List<DeudaMatriculaResponse> listarDeudas(
            @PathVariable Long id,
            @RequestParam Integer anio) {
        return alumnoService.listarDeudasMatricula(id, anio);
    }

    @GetMapping("/documento/{numero}")
    @PreAuthorize("hasPermission(null, 'ALUMNOS', 'VER')")
    public List<AlumnoBusquedaResponse> buscarPorDocumento(@PathVariable String numero) {
        return alumnoService.buscarPorDocumento(numero);
    }

    @GetMapping("/buscar")
    @PreAuthorize("hasPermission(null, 'ALUMNOS', 'VER')")
    public AlumnoResponse buscarPorDni(
            @RequestParam @NotBlank @Pattern(regexp = "\\d{8}", message = "El DNI debe tener 8 dígitos") String dni) {
        return alumnoService.buscarPorDni(dni);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasPermission(null, 'ALUMNOS', 'ELIMINAR')")
    public void eliminar(@PathVariable Long id) {
        alumnoService.eliminar(id);
    }
}