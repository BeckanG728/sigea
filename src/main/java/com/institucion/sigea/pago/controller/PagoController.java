package com.institucion.sigea.pago.controller;

import com.institucion.sigea.pago.dto.request.RegistrarPagoRequest;
import com.institucion.sigea.pago.dto.response.CuotaDeudaResponse;
import com.institucion.sigea.pago.dto.response.HistorialGeneralResponse;
import com.institucion.sigea.pago.dto.response.PagoResponse;
import com.institucion.sigea.pago.service.PagoService;
import com.institucion.sigea.pago.service.PagoTransaccionService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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
    @PreAuthorize("hasPermission(null, 'PAGO_REGISTRAR', 'CREAR')")
    public PagoResponse registrarPago(@Valid @RequestBody RegistrarPagoRequest request) {
        return pagoTransaccionService.registrarPago(request);
    }

    @GetMapping("/deudas")
    @PreAuthorize("hasPermission(null, 'DEUDA_HISTORIAL', 'VER')")
    public List<CuotaDeudaResponse> listarDeudas(@RequestParam Long codAlumno) {
        return pagoService.listarDeudas(codAlumno);
    }

    @GetMapping("/historial")
    @PreAuthorize("hasPermission(null, 'DEUDA_HISTORIAL', 'VER')")
    public HistorialGeneralResponse listarHistorial(@PageableDefault(size = 8) Pageable pageable) {
        return pagoService.listarHistorialGeneral(pageable);
    }
}
