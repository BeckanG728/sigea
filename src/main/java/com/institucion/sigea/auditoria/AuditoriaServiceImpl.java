package com.institucion.sigea.auditoria;

import com.institucion.sigea.core.enums.TipoOperacionAuditoria;
import com.institucion.sigea.usuario.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class AuditoriaServiceImpl implements AuditoriaService {

    private final AuditoriaRepository auditoriaRepository;
    private final UsuarioRepository usuarioRepository;

    @Override
    @Transactional
    public void registrar(AuditoriaEntity auditoria) {
        auditoriaRepository.save(auditoria);
    }

    @Override
    @Transactional
    public void registrarLogin(Long idUsuario, String ip, String equipo, String navegador) {
        AuditoriaEntity entity = new AuditoriaEntity();
        if (idUsuario != null) {
            entity.setUsuario(usuarioRepository.getReferenceById(idUsuario));
        }
        entity.setModulo("auth");
        entity.setOperacion(TipoOperacionAuditoria.LOGIN);
        entity.setFechaHora(Instant.now());
        entity.setIpOrigen(ip);
        entity.setEquipo(equipo);
        entity.setNavegador(navegador);
        auditoriaRepository.save(entity);
    }

    @Override
    @Transactional
    public void registrarIntentoFallido(Long idUsuario, String ip, String equipo, String navegador) {
        AuditoriaEntity entity = new AuditoriaEntity();
        if (idUsuario != null) {
            entity.setUsuario(usuarioRepository.getReferenceById(idUsuario));
        }
        entity.setModulo("auth");
        entity.setOperacion(TipoOperacionAuditoria.LOGIN_FAILED);
        entity.setFechaHora(Instant.now());
        entity.setIpOrigen(ip);
        entity.setEquipo(equipo);
        entity.setNavegador(navegador);
        auditoriaRepository.save(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public int contarIntentosFallidos(Long idUsuario, int minutos) {
        Instant desde = Instant.now().minus(minutos, ChronoUnit.MINUTES);
        return auditoriaRepository.countByOperacionAndFechaHoraAfter(
                TipoOperacionAuditoria.LOGIN_FAILED, desde, idUsuario);
    }
}
