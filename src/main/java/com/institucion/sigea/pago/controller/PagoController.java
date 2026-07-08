package com.institucion.sigea.pago.controller;

import com.institucion.sigea.pago.dto.request.RegistrarPagoRequest;
import com.institucion.sigea.pago.dto.response.CuotaDeudaResponse;
import com.institucion.sigea.pago.dto.response.PagoResponse;
import com.institucion.sigea.pago.service.PagoService;
import com.institucion.sigea.pago.service.PagoTransaccionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/pagos") // sin /api: el context-path global ya lo agrega
public class PagoController {

    private final PagoService pagoService;
    private final PagoTransaccionService pagoTransaccionService;

    public PagoController(PagoService pagoService, PagoTransaccionService pagoTransaccionService) {
        this.pagoService = pagoService;
        this.pagoTransaccionService = pagoTransaccionService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasPermission(null, 'PAGO', 'CREAR')")
    public PagoResponse registrarPago(@Valid @RequestBody RegistrarPagoRequest request) {
        return pagoTransaccionService.registrarPago(request);
    }

    @GetMapping("/deudas")
    @PreAuthorize("hasPermission(null, 'PAGO', 'VER')")
    public List<CuotaDeudaResponse> listarDeudas(@RequestParam Long codAlumno) {
        return pagoService.listarDeudas(codAlumno);
    }
}
