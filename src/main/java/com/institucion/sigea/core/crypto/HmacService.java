package com.institucion.sigea.core.crypto;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;

@Service
public class HmacService {

    private static final String ALGORITHM = "HmacSHA256";

    private final String secretKey;

    public HmacService(@Value("${app.security.hmac-key}") String secretKey) {
        this.secretKey = secretKey;
    }

    public String generarHash(String texto) {

        if (texto == null || texto.isBlank()) {
            throw new IllegalArgumentException("El texto para hash no puede estar vacío");
        }

        try {
            Mac mac = Mac.getInstance(ALGORITHM);

            SecretKeySpec keySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), ALGORITHM);

            mac.init(keySpec);

            byte[] hash = mac.doFinal(texto.trim().getBytes(StandardCharsets.UTF_8));

            return HexFormat.of().formatHex(hash);

        } catch (Exception e) {
            throw new IllegalStateException("Error generando HMAC-SHA256", e);
        }
    }
}