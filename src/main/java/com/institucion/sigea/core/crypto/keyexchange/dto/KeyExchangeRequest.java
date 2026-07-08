package com.institucion.sigea.core.crypto.keyexchange.dto;

import jakarta.validation.constraints.NotBlank;

public record KeyExchangeRequest(
        @NotBlank(message = "La clave pública del cliente es obligatoria")
        String clientPublicKey // Base64, formato SPKI (X.509), curva secp256r1
) {}
