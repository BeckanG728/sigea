package com.institucion.sigea.matricula.controller;

import com.institucion.sigea.matricula.dto.request.MatriculaPreviewRequest;
import com.institucion.sigea.matricula.dto.request.MatriculaRegisterRequest;
import com.institucion.sigea.matricula.dto.response.MatriculaPreviewResponse;
import com.institucion.sigea.matricula.dto.response.MatriculaRegisterResponse;
import com.institucion.sigea.matricula.service.MatriculaModuleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/matricula")
@RequiredArgsConstructor
public class MatriculaModuleController {

    private final MatriculaModuleService matriculaModuleService;

    @PostMapping("/preview")
    @PreAuthorize("hasPermission(null, 'MATRICULA', 'CREAR')")
    public MatriculaPreviewResponse preview(@Valid @RequestBody MatriculaPreviewRequest request) {
        return matriculaModuleService.preview(request);
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasPermission(null, 'MATRICULA', 'CREAR')")
    public MatriculaRegisterResponse register(@Valid @RequestBody MatriculaRegisterRequest request) {
        return matriculaModuleService.register(request);
    }
}