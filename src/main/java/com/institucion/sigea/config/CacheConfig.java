package com.institucion.sigea.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {

    public static final String CACHE_PERMISOS_POR_ROL = "permisosPorRol";
    public static final String CACHE_SESION_2FA_PENDIENTE = "sesion2faPendiente";
    public static final String CACHE_PARAMETROS_SISTEMA = "parametrosSistema";
    public static final String CACHE_USUARIOS_DESACTIVADOS = "usuariosDesactivados";

    public static final String CACHE_SESION_ECDH = "sesionEcdh";

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager manager = new CaffeineCacheManager();

        manager.registerCustomCache(CACHE_PERMISOS_POR_ROL,
                Caffeine.newBuilder()
                        .maximumSize(50)
                        .build());

        manager.registerCustomCache(CACHE_SESION_ECDH,
                Caffeine.newBuilder()
                        .expireAfterWrite(5, TimeUnit.MINUTES)
                        .maximumSize(1000)
                        .build());

        manager.registerCustomCache(CACHE_SESION_2FA_PENDIENTE,
                Caffeine.newBuilder()
                        .expireAfterWrite(5, TimeUnit.MINUTES)
                        .maximumSize(1000)
                        .build());

        manager.registerCustomCache(CACHE_PARAMETROS_SISTEMA,
                Caffeine.newBuilder()
                        .expireAfterWrite(30, TimeUnit.MINUTES)
                        .maximumSize(100)
                        .build());

        manager.registerCustomCache(CACHE_USUARIOS_DESACTIVADOS,
                Caffeine.newBuilder()
                        .expireAfterWrite(15, TimeUnit.MINUTES)
                        .maximumSize(10000)
                        .build());

        return manager;
    }
}
