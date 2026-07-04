package com.institucion.sigea.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "aes")
public record AesProperties(String secret) {
}
