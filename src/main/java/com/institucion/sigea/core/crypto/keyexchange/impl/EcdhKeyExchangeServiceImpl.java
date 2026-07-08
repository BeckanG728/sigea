package com.institucion.sigea.core.crypto.keyexchange.impl;

import com.institucion.sigea.config.CacheConfig;
import com.institucion.sigea.core.crypto.keyexchange.EcdhKeyExchangeService;
import com.institucion.sigea.core.crypto.keyexchange.dto.KeyExchangeRequest;
import com.institucion.sigea.core.crypto.keyexchange.dto.KeyExchangeResponse;
import com.institucion.sigea.core.exception.BusinessException;
import com.institucion.sigea.core.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import javax.crypto.KeyAgreement;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EcdhKeyExchangeServiceImpl implements EcdhKeyExchangeService {

    private static final String CURVA = "secp256r1";
    private static final int LONGITUD_CLAVE_BYTES = 32; // AES-256
    private static final long TTL_SEGUNDOS = 300; // igual al TTL de CACHE_SESION_ECDH
    private static final byte[] INFO_HKDF = "SIGEA-ECDH-AES256".getBytes(StandardCharsets.UTF_8);

    private final CacheManager cacheManager;

    @Override
    public KeyExchangeResponse iniciar(KeyExchangeRequest request) {
        try {
            PublicKey clavePublicaCliente = decodificarClavePublica(request.clientPublicKey());

            KeyPairGenerator generador = KeyPairGenerator.getInstance("EC");
            generador.initialize(new ECGenParameterSpec(CURVA));
            KeyPair parServidor = generador.generateKeyPair();

            KeyAgreement acuerdo = KeyAgreement.getInstance("ECDH");
            acuerdo.init(parServidor.getPrivate());
            acuerdo.doPhase(clavePublicaCliente, true);
            byte[] secretoCompartido = acuerdo.generateSecret();

            String sessionId = UUID.randomUUID().toString();
            byte[] claveDerivada = hkdf(secretoCompartido,
                    sessionId.getBytes(StandardCharsets.UTF_8), INFO_HKDF, LONGITUD_CLAVE_BYTES);

            Cache cache = cacheManager.getCache(CacheConfig.CACHE_SESION_ECDH);
            if (cache != null) {
                cache.put(sessionId, new SecretKeySpec(claveDerivada, "AES"));
            }

            String clavePublicaServidorB64 = Base64.getEncoder().encodeToString(parServidor.getPublic().getEncoded());
            return new KeyExchangeResponse(sessionId, clavePublicaServidorB64, TTL_SEGUNDOS);
        } catch (GeneralSecurityException e) {
            throw new BusinessException(ErrorCode.KEYEXCHANGE_INVALID_KEY,
                    "No se pudo completar el intercambio de claves");
        }
    }

    @Override
    public SecretKey obtenerClaveSesion(String sessionId) {
        Cache cache = cacheManager.getCache(CacheConfig.CACHE_SESION_ECDH);
        SecretKey clave = cache != null ? cache.get(sessionId, SecretKey.class) : null;
        if (clave == null) {
            throw new BusinessException(ErrorCode.KEYEXCHANGE_SESSION_EXPIRED,
                    "La sesión de intercambio de claves expiró o no existe");
        }
        return clave;
    }

    @Override
    public void invalidar(String sessionId) {
        Cache cache = cacheManager.getCache(CacheConfig.CACHE_SESION_ECDH);
        if (cache != null) {
            cache.evict(sessionId);
        }
    }

    private PublicKey decodificarClavePublica(String base64) throws GeneralSecurityException {
        try {
            byte[] bytes = Base64.getDecoder().decode(base64);
            KeyFactory factory = KeyFactory.getInstance("EC");
            return factory.generatePublic(new X509EncodedKeySpec(bytes));
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.KEYEXCHANGE_INVALID_KEY,
                    "Clave pública inválida (Base64 malformado)");
        }
    }

    private byte[] hkdf(byte[] ikm, byte[] salt, byte[] info, int longitudBytes) throws GeneralSecurityException {
        Mac hmac = Mac.getInstance("HmacSHA256");

        hmac.init(new SecretKeySpec(salt, "HmacSHA256"));
        byte[] prk = hmac.doFinal(ikm);

        ByteArrayOutputStream okm = new ByteArrayOutputStream();
        byte[] t = new byte[0];
        int contador = 1;
        while (okm.size() < longitudBytes) {
            hmac.init(new SecretKeySpec(prk, "HmacSHA256"));
            hmac.update(t);
            hmac.update(info);
            hmac.update((byte) contador);
            t = hmac.doFinal();
            okm.writeBytes(t);
            contador++;
        }
        return Arrays.copyOf(okm.toByteArray(), longitudBytes);
    }
}
