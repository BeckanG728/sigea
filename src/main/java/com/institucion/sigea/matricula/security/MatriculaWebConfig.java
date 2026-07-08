package com.institucion.sigea.matricula.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class MatriculaWebConfig implements WebMvcConfigurer {

    private final TwoFaClaimInterceptor twoFaClaimInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(twoFaClaimInterceptor)
                .addPathPatterns("/matriculas");
    }
}
