package com.institucion.sigea.auth.service.impl;

import com.institucion.sigea.auth.dto.internal.GenerarSecretoResult;
import com.institucion.sigea.auth.service.TotpService;
import com.institucion.sigea.config.properties.TotpProperties;
import com.institucion.sigea.core.exception.BusinessException;
import com.institucion.sigea.core.exception.ErrorCode;
import dev.samstevens.totp.code.HashingAlgorithm;
import dev.samstevens.totp.code.CodeVerifier;
import dev.samstevens.totp.code.DefaultCodeVerifier;
import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.secret.SecretGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TotpServiceImpl implements TotpService {

    private final TotpProperties totpProperties;

    private final SecretGenerator secretGenerator = new DefaultSecretGenerator();
    private final DefaultCodeGenerator codeGenerator = new DefaultCodeGenerator();
    private final SystemTimeProvider timeProvider = new SystemTimeProvider();
    private final CodeVerifier codeVerifier = new DefaultCodeVerifier(codeGenerator, timeProvider);

    @Override
    public void verificarCodigo(String secret, String codigo) {
        if (!codeVerifier.isValidCode(secret, codigo)) {
            throw new BusinessException(ErrorCode.INVALID_TOTP,
                    "El código de verificación es inválido o expiró");
        }
    }

    @Override
    public GenerarSecretoResult generarSecreto(String username) {
        String secret = secretGenerator.generate();
        String issuer = totpProperties.issuer();
        String uri = new QrData.Builder()
                .label(issuer + ":" + username)
                .secret(secret)
                .issuer(issuer)
                .algorithm(HashingAlgorithm.SHA1)
                .digits(6)
                .period(30)
                .build()
                .getUri();
        return new GenerarSecretoResult(secret, uri);
    }
}
