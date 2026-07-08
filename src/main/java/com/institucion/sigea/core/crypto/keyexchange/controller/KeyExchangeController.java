package com.institucion.sigea.core.crypto.keyexchange.controller;

import com.institucion.sigea.core.crypto.keyexchange.EcdhKeyExchangeService;
import com.institucion.sigea.core.crypto.keyexchange.dto.KeyExchangeRequest;
import com.institucion.sigea.core.crypto.keyexchange.dto.KeyExchangeResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/crypto/key-exchange")
@RequiredArgsConstructor
public class KeyExchangeController {

    private final EcdhKeyExchangeService keyExchangeService;

    @PostMapping
    public ResponseEntity<KeyExchangeResponse> iniciar(@RequestBody @Valid KeyExchangeRequest request) {
        return ResponseEntity.ok(keyExchangeService.iniciar(request));
    }
}
