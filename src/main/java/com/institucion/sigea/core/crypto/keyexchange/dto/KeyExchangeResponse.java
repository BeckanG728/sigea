package com.institucion.sigea.core.crypto.keyexchange.dto;

public record KeyExchangeResponse(
        String sessionId,
        String serverPublicKey, // Base64, formato SPKI
        long expiraEnSegundos
) {}
