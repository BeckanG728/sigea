package com.institucion.sigea.matricula.controller;

import com.institucion.sigea.matricula.dto.request.MatriculaPreviewRequest;
import com.institucion.sigea.matricula.dto.request.MatriculaRegisterRequest;
import com.institucion.sigea.matricula.dto.response.DeudaAnteriorResponse;
import com.institucion.sigea.matricula.dto.response.MatriculaPreviewResponse;
import com.institucion.sigea.matricula.dto.response.MatriculaRegisterResponse;
import com.institucion.sigea.matricula.service.MatriculaModuleService;
import com.institucion.sigea.pago.service.PagoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/matricula")
@RequiredArgsConstructor
public class MatriculaModuleController {

    private final MatriculaModuleService matriculaModuleService;
    private final PagoService pagoService;

    @PostMapping("/preview")
    @PreAuthorize("hasPermission(null, 'MATRICULA_REGISTRAR', 'CREAR')")
    public MatriculaPreviewResponse preview(@Valid @RequestBody MatriculaPreviewRequest request) {
        return matriculaModuleService.preview(request);
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasPermission(null, 'MATRICULA_REGISTRAR', 'CREAR')")
    public MatriculaRegisterResponse register(@Valid @RequestBody MatriculaRegisterRequest request) {
        return matriculaModuleService.register(request);
    }

    @GetMapping("/deudas")
    @PreAuthorize("hasPermission(null, 'DEUDA_HISTORIAL', 'VER')")
    public List<DeudaAnteriorResponse> deudas(@RequestParam Long alumnoId) {
        return pagoService.listarDeudas(alumnoId).stream()
                .map(c -> new DeudaAnteriorResponse(
                        c.nombreConcepto(),
                        c.montoPagar(),
                        c.estadoCuota().name().toLowerCase()
                ))
                .toList();
    }
}