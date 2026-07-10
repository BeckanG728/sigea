package com.institucion.sigea.auditoria;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.institucion.sigea.core.enums.TipoOperacionAuditoria;
import com.institucion.sigea.security.jwt.JwtPrincipal;
import com.institucion.sigea.usuario.repository.UsuarioRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import java.time.Instant;

@Aspect
@Component
@Order(Ordered.LOWEST_PRECEDENCE)
@RequiredArgsConstructor
public class AuditoriaAspect {

    private final AuditoriaRepository auditoriaRepository;
    private final UsuarioRepository usuarioRepository;
    private final ObjectMapper objectMapper;
    private static final Set<String> CAMPOS_CIFRADOS = Set.of(
            "numeroDocumento", "fechaNacimiento", "totpSecret", "nombreUsuario"
    );
    private static final String MASCARA = "***CIFRADO***";

    @Around("@annotation(auditable)")
    public Object auditar(ProceedingJoinPoint pjp, Auditable auditable) throws Throwable {
        TipoOperacionAuditoria operacion = auditable.operacion();

        Object firstArg = pjp.getArgs().length > 0 ? pjp.getArgs()[0] : null;

        String valorAnterior = null;
        if (operacion == TipoOperacionAuditoria.UPDATE
                || operacion == TipoOperacionAuditoria.DELETE) {
            if (firstArg != null) {
                valorAnterior = toJson(firstArg);
            }
        }

        Object result = pjp.proceed();

        String valorNuevo = null;
        if (operacion != TipoOperacionAuditoria.DELETE && result != null) {
            valorNuevo = toJson(result);
        }

        String ip = null;
        String navegador = null;
        try {
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder
                    .currentRequestAttributes()).getRequest();
            ip = request.getRemoteAddr();
            navegador = request.getHeader("User-Agent");
        } catch (Exception e) {
            // Sin request HTTP (seeder, tareas programadas, etc.)
        }

        AuditoriaEntity entity = new AuditoriaEntity();
        entity.setModulo(auditable.modulo());
        entity.setOperacion(operacion);
        entity.setTablaAfectada(extraerTabla(firstArg, result));
        entity.setCodigoRegistro(extraerCodigoRegistro(firstArg, result));
        entity.setValorAnterior(valorAnterior);
        entity.setValorNuevo(valorNuevo);
        entity.setFechaHora(Instant.now());
        entity.setIpOrigen(ip);
        entity.setNavegador(navegador);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof JwtPrincipal principal) {
            entity.setUsuario(usuarioRepository.getReferenceById(principal.userId()));
        }

        auditoriaRepository.save(entity);

        return result;
    }

    private String toJson(Object obj) {
        try {
            Map<String, Object> nodo = objectMapper.convertValue(obj, new TypeReference<>() {
            });
            for (String campo : CAMPOS_CIFRADOS) {
                if (nodo.containsKey(campo)) {
                    nodo.put(campo, MASCARA);
                }
            }
            return objectMapper.writeValueAsString(nodo);
        } catch (Exception e) {
            return null;
        }
    }

    private String extraerTabla(Object firstArg, Object result) {
        Object source = result != null ? result : firstArg;
        return source != null ? source.getClass().getSimpleName() : null;
    }

    private String extraerCodigoRegistro(Object firstArg, Object result) {
        Object source = result != null ? result : firstArg;
        if (source == null) return null;

        // eliminar(Long id) -> el propio argumento YA es el identificador
        if (source instanceof Long || source instanceof Integer) {
            return source.toString();
        }

        String codigo = invocarAccessor(source, "getCodigo", "codigo");
        if (codigo != null) return codigo;

        return invocarAccessor(source, "getId", "id");
    }

    private String invocarAccessor(Object source, String nombreGetter, String nombreRecord) {
        for (String nombreMetodo : new String[]{nombreGetter, nombreRecord}) {
            try {
                Object valor = source.getClass().getMethod(nombreMetodo).invoke(source);
                if (valor != null) return valor.toString();
            } catch (NoSuchMethodException ignored) {
                
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }
}
