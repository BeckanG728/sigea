package com.institucion.sigea.parametro.service;

public interface ParametroService {
    String obtener(String clave);
    String actualizar(String clave, String valor);
    void eliminar(String clave);
}