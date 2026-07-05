package com.institucion.sigea.auditoria;

public interface AuditoriaService {

    void registrar(AuditoriaEntity auditoria);

    void registrarLogin(Long idUsuario, String ip, String equipo, String navegador);

    void registrarIntentoFallido(Long idUsuario, String ip, String equipo, String navegador);

    int contarIntentosFallidos(Long idUsuario, int minutos);
}
