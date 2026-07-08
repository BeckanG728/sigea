package com.institucion.sigea.core.crypto.keyexchange;

import com.institucion.sigea.core.crypto.keyexchange.dto.KeyExchangeRequest;
import com.institucion.sigea.core.crypto.keyexchange.dto.KeyExchangeResponse;

import javax.crypto.SecretKey;

public interface EcdhKeyExchangeService {

    /**
     * Completa el intercambio ECDH con la clave pública del cliente y deja
     * la clave de sesión derivada lista en caché.
     */
    KeyExchangeResponse iniciar(KeyExchangeRequest request);

    /**
     * Recupera la clave AES derivada para una sesión ECDH activa.
     * Lanza BusinessException(KEYEXCHANGE_SESSION_EXPIRED) si no existe o expiró.
     */
    SecretKey obtenerClaveSesion(String sessionId);

    /** Invalida la sesión (ej. tras usarla una sola vez). */
    void invalidar(String sessionId);
}
