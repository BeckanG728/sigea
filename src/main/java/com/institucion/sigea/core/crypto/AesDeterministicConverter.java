package com.institucion.sigea.core.crypto;

import com.institucion.sigea.config.properties.AesProperties;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;

/**
 * Cifrado AES/GCM con IV DETERMINÍSTICO (derivado del propio texto plano vía
 * HMAC-SHA256 con la misma clave), a diferencia de AesConverter (IV aleatorio).
 * Mismo texto + misma clave -> mismo ciphertext siempre. Se usa exclusivamente
 * en columnas donde se necesita Unique Key o búsqueda exacta (numeroDocumento,
 * nombre_usuario). NO usar en campos donde el patrón de repetición de valores
 * sea sensible (ahí sigue aplicando AesConverter).
 */

//No olvidar borrar este comentario

@Component
@RequiredArgsConstructor
@Converter
public class AesDeterministicConverter implements AttributeConverter<String, String> {

    private static final String CIPHER_TRANSFORMATION = "AES/GCM/NoPadding";
    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final int GCM_IV_LENGTH_BYTES = 12;
    private static final int GCM_TAG_LENGTH_BITS = 128;

    private final AesProperties aesProperties;

    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (attribute == null) return null;
        try {
            byte[] iv = derivarIvDeterministico(attribute);

            Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey(), new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv));
            byte[] cipherText = cipher.doFinal(attribute.getBytes(StandardCharsets.UTF_8));

            byte[] ivAndCipherText = new byte[iv.length + cipherText.length];
            System.arraycopy(iv, 0, ivAndCipherText, 0, iv.length);
            System.arraycopy(cipherText, 0, ivAndCipherText, iv.length, cipherText.length);

            return Base64.getEncoder().encodeToString(ivAndCipherText);
        } catch (Exception e) {
            throw new IllegalStateException("Error al cifrar el atributo con AES determinístico", e);
        }
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        try {
            byte[] ivAndCipherText = Base64.getDecoder().decode(dbData);
            byte[] iv = Arrays.copyOfRange(ivAndCipherText, 0, GCM_IV_LENGTH_BYTES);
            byte[] cipherText = Arrays.copyOfRange(ivAndCipherText, GCM_IV_LENGTH_BYTES, ivAndCipherText.length);

            Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, secretKey(), new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv));

            return new String(cipher.doFinal(cipherText), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("Error al descifrar el atributo con AES determinístico", e);
        }
    }

    private byte[] derivarIvDeterministico(String plainText) throws Exception {
        Mac mac = Mac.getInstance(HMAC_ALGORITHM);
        mac.init(secretKey());
        byte[] hash = mac.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
        return Arrays.copyOf(hash, GCM_IV_LENGTH_BYTES); // el IV se guarda igual que en AesConverter, no hace falta "rederivarlo" al descifrar
    }

    private SecretKeySpec secretKey() {
        byte[] keyBytes = Base64.getDecoder().decode(aesProperties.secret());
        return new SecretKeySpec(keyBytes, "AES");
    }
}