package com.institucion.sigea.parametro.service.impl;

import com.institucion.sigea.config.CacheConfig;
import com.institucion.sigea.core.exception.BusinessException;
import com.institucion.sigea.core.exception.ErrorCode;
import com.institucion.sigea.parametro.entity.Parametro;
import com.institucion.sigea.parametro.repository.ParametroRepository;
import com.institucion.sigea.parametro.service.ParametroService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ParametroServiceImpl implements ParametroService {

    private final ParametroRepository parametroRepository;

    @Override
    @Cacheable(value = CacheConfig.CACHE_PARAMETROS_SISTEMA, key = "#clave")
    public String obtener(String clave) {
        return parametroRepository.findByClave(clave)
                .map(Parametro::getValor)
                .orElseThrow(() -> new BusinessException(ErrorCode.INTERNAL_ERROR,
                        "Parámetro no configurado: " + clave));
    }

    @Override
    @Transactional
    @CachePut(value = CacheConfig.CACHE_PARAMETROS_SISTEMA, key = "#clave")
    public String actualizar(String clave, String valor) {
        Parametro parametro = parametroRepository.findByClave(clave)
                .orElseThrow(() -> new BusinessException(ErrorCode.INTERNAL_ERROR,
                        "Parámetro no configurado: " + clave));
        parametro.setValor(valor);
        return valor;
    }
}