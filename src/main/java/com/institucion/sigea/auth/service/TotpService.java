package com.institucion.sigea.auth.service;

import com.institucion.sigea.auth.dto.internal.GenerarSecretoResult;

public interface TotpService {

    void verificarCodigo(String secret, String codigo);

    GenerarSecretoResult generarSecreto(String username);

    String generarQrUri(String secret, String username);
}
