package com.institucion.sigea.aula.service.impl;

import com.institucion.sigea.aula.entity.Aula;
import com.institucion.sigea.aula.repository.AnioAcademicoRepository;
import com.institucion.sigea.aula.repository.AulaRepository;
import com.institucion.sigea.aula.repository.GradoRepository;
import com.institucion.sigea.aula.repository.NivelRepository;
import com.institucion.sigea.core.exception.BusinessException;
import com.institucion.sigea.core.exception.ErrorCode;
import com.institucion.sigea.parametro.service.ParametroService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AulaServiceImplTest {

    @Mock private AulaRepository aulaRepository;
    @Mock private AnioAcademicoRepository anioAcademicoRepository;
    @Mock private NivelRepository nivelRepository;
    @Mock private GradoRepository gradoRepository;
    @Mock private ParametroService parametroService;

    @InjectMocks
    private AulaServiceImpl aulaService;

    private Aula aulaExistente;

    @BeforeEach
    void setUp() {
        aulaExistente = new Aula();
        aulaExistente.setSeccion("A");
        aulaExistente.setCapacidadMaxima((short) 30);
        // el id y estado vienen de BaseEntity; estado por defecto arranca en true
    }

    @Test
    void eliminar_marcaEstadoFalse_yGuardaSinBorrarFisicamente() {
        when(aulaRepository.findById(1L)).thenReturn(Optional.of(aulaExistente));

        aulaService.eliminar(1L);

        assertFalse(aulaExistente.isEstado(), "El aula debe quedar con estado=false");
        verify(aulaRepository, times(1)).save(aulaExistente);
        verify(aulaRepository, never()).deleteById(any()); // nunca debe usar delete físico
        verify(aulaRepository, never()).delete(any());
    }

    @Test
    void eliminar_aulaInexistente_lanzaBusinessException() {
        when(aulaRepository.findById(99L)).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class,
                () -> aulaService.eliminar(99L));

        assertEquals(ErrorCode.AULA_NO_ENCONTRADA, ex.getErrorCode());
        verify(aulaRepository, never()).save(any());
    }
}