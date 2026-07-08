package com.institucion.sigea.core.crypto.keyexchange;

public interface EnvelopeCipherService {
    String cifrar(String sessionId, String textoPlano);
    String descifrar(String sessionId, String textoCifrado);
}