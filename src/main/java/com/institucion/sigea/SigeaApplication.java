package com.institucion.sigea;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class SigeaApplication {

    public static void main(String[] args) {
        SpringApplication.run(SigeaApplication.class, args);
    }

}
