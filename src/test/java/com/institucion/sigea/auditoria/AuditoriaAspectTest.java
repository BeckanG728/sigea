package com.institucion.sigea.auditoria;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.institucion.sigea.core.enums.TipoOperacionAuditoria;
import com.institucion.sigea.usuario.repository.UsuarioRepository;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditoriaAspectTest {

    @Mock private AuditoriaRepository auditoriaRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private ProceedingJoinPoint proceedingJoinPoint;
    @Mock private Auditable auditable;

    private AuditoriaAspect auditoriaAspect;

    @BeforeEach
    void setUp() {
        auditoriaAspect = new AuditoriaAspect(auditoriaRepository, usuarioRepository, new ObjectMapper());
    }

    @Test
    void auditar_operacionInsert_guardaFilaDeAuditoriaConModuloYOperacionCorrectos() throws Throwable {
        when(auditable.modulo()).thenReturn("aula");
        when(auditable.operacion()).thenReturn(TipoOperacionAuditoria.INSERT);
        when(proceedingJoinPoint.getArgs()).thenReturn(new Object[]{});
        when(proceedingJoinPoint.proceed()).thenReturn(null);

        auditoriaAspect.auditar(proceedingJoinPoint, auditable);

        ArgumentCaptor<AuditoriaEntity> captor = ArgumentCaptor.forClass(AuditoriaEntity.class);
        verify(auditoriaRepository, times(1)).save(captor.capture());

        AuditoriaEntity guardada = captor.getValue();
        assertEquals("aula", guardada.getModulo());
        assertEquals(TipoOperacionAuditoria.INSERT, guardada.getOperacion());
    }
}