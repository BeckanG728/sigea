package com.institucion.sigea.auth;

import com.institucion.sigea.auth.dto.internal.GenerarSecretoResult;
import com.institucion.sigea.auth.service.TotpService;
import com.institucion.sigea.auth.service.impl.TotpServiceImpl;
import com.institucion.sigea.config.properties.TotpProperties;
import com.institucion.sigea.core.exception.BusinessException;
import com.institucion.sigea.core.exception.ErrorCode;
import dev.samstevens.totp.code.CodeGenerator;
import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.exceptions.CodeGenerationException;
import dev.samstevens.totp.time.SystemTimeProvider;
import dev.samstevens.totp.time.TimeProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TotpServiceImplTest {

    private static final String TEST_ISSUER = "SIGEA-TEST";
    private static final String TEST_USERNAME = "jperez";

    private TotpService totpService;

    @BeforeEach
    void setUp() {
        TotpProperties totpProperties = new TotpProperties(TEST_ISSUER);
        totpService = new TotpServiceImpl(totpProperties);
    }

    @Test
    void generarSecreto_deberiaRetornarSecretoYUriValidos() {
        GenerarSecretoResult result = totpService.generarSecreto(TEST_USERNAME);

        assertNotNull(result);
        assertNotNull(result.secretRaw());
        assertFalse(result.secretRaw().isBlank());
        assertNotNull(result.uri());
        assertTrue(result.uri().startsWith("otpauth://totp/"));
        assertTrue(result.uri().contains("issuer=" + TEST_ISSUER));
    }

    @Test
    void verificarCodigo_deberiaAceptarCodigoValido() throws CodeGenerationException {
        GenerarSecretoResult result = totpService.generarSecreto(TEST_USERNAME);

        CodeGenerator generator = new DefaultCodeGenerator();
        TimeProvider timeProvider = new SystemTimeProvider();
        long currentPeriod = Math.floorDiv(timeProvider.getTime(), 30);
        String codigoValido = generator.generate(result.secretRaw(), currentPeriod);

        assertDoesNotThrow(() -> totpService.verificarCodigo(result.secretRaw(), codigoValido));
    }

    @Test
    void verificarCodigo_deberiaRechazarCodigoInvalido() {
        GenerarSecretoResult result = totpService.generarSecreto(TEST_USERNAME);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> totpService.verificarCodigo(result.secretRaw(), "000000"));

        assertEquals(ErrorCode.INVALID_TOTP, exception.getErrorCode());
    }

    @Test
    void generarSecreto_deberiaUsarIssuerDeProperties() {
        GenerarSecretoResult result = totpService.generarSecreto("alopez");

        assertTrue(result.uri().contains("issuer=" + TEST_ISSUER));
    }

    @Test
    void generarSecreto_deberiaGenerarDistintosSecretos() {
        GenerarSecretoResult r1 = totpService.generarSecreto(TEST_USERNAME);
        GenerarSecretoResult r2 = totpService.generarSecreto(TEST_USERNAME);

        assertNotEquals(r1.secretRaw(), r2.secretRaw());
        assertNotEquals(r1.uri(), r2.uri());
    }
}
