package com.institucion.sigea.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "totp")
public record TotpProperties(String issuer) {
}
