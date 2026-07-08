package com.institucion.sigea.auditoria;

import java.time.Instant;
import java.util.List;

public interface AuditoriaService {

    void registrar(AuditoriaEntity auditoria);

    void registrarLogin(Long idUsuario, String ip, String equipo, String navegador);

    void registrarIntentoFallido(Long idUsuario, String ip, String equipo, String navegador);

    int contarIntentosFallidos(Long idUsuario, int minutos);

    List<AuditoriaEntity> buscar(Long usuarioId, String modulo, Instant desde, Instant hasta);
}
