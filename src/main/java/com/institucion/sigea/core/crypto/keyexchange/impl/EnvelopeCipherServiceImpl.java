package com.institucion.sigea.core.crypto.keyexchange.impl;

import com.institucion.sigea.core.crypto.keyexchange.EcdhKeyExchangeService;
import com.institucion.sigea.core.crypto.keyexchange.EnvelopeCipherService;
import com.institucion.sigea.core.exception.BusinessException;
import com.institucion.sigea.core.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class EnvelopeCipherServiceImpl implements EnvelopeCipherService {

    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH_BYTES = 12;
    private static final int GCM_TAG_LENGTH_BITS = 128;

    private final EcdhKeyExchangeService keyExchangeService;

    @Override
    public String cifrar(String sessionId, String textoPlano) {
        try {
            SecretKey clave = keyExchangeService.obtenerClaveSesion(sessionId);
            byte[] iv = new byte[GCM_IV_LENGTH_BYTES];
            new SecureRandom().nextBytes(iv);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, clave, new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv));
            byte[] cipherText = cipher.doFinal(textoPlano.getBytes(StandardCharsets.UTF_8));

            byte[] ivYCipherText = new byte[iv.length + cipherText.length];
            System.arraycopy(iv, 0, ivYCipherText, 0, iv.length);
            System.arraycopy(cipherText, 0, ivYCipherText, iv.length, cipherText.length);

            return Base64.getEncoder().encodeToString(ivYCipherText);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Error al cifrar el payload de sesión", e);
        }
    }

    @Override
    public String descifrar(String sessionId, String textoCifrado) {
        try {
            SecretKey clave = keyExchangeService.obtenerClaveSesion(sessionId);
            byte[] ivYCipherText = Base64.getDecoder().decode(textoCifrado);

            byte[] iv = Arrays.copyOfRange(ivYCipherText, 0, GCM_IV_LENGTH_BYTES);
            byte[] cipherText = Arrays.copyOfRange(ivYCipherText, GCM_IV_LENGTH_BYTES, ivYCipherText.length);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, clave, new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv));

            return new String(cipher.doFinal(cipherText), StandardCharsets.UTF_8);
        } catch (GeneralSecurityException e) {
            throw new BusinessException(ErrorCode.KEYEXCHANGE_INVALID_KEY,
                    "No se pudo descifrar el payload recibido");
        }
    }
}
