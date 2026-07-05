package com.institucion.sigea.usuario.service.impl;

import com.institucion.sigea.core.exception.BusinessException;
import com.institucion.sigea.core.exception.ErrorCode;
import com.institucion.sigea.usuario.dto.request.PermisoItem;
import com.institucion.sigea.usuario.dto.response.PermisoInfo;
import com.institucion.sigea.usuario.entity.Funcionalidad;
import com.institucion.sigea.usuario.entity.Rol;
import com.institucion.sigea.usuario.entity.RolFuncionalidad;
import com.institucion.sigea.usuario.repository.FuncionalidadRepository;
import com.institucion.sigea.usuario.repository.RolFuncionalidadRepository;
import com.institucion.sigea.usuario.repository.RolRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PermisoServiceImplTest {

    private static final Long ID_ROL = 1L;
    private static final Long ID_FUNC_ALUMNOS = 10L;
    private static final Long ID_FUNC_AULAS = 20L;
    private static final Long ID_FUNC_MATRICULA = 30L;

    @Mock private RolRepository rolRepository;
    @Mock private FuncionalidadRepository funcionalidadRepository;
    @Mock private RolFuncionalidadRepository rolFuncionalidadRepository;

    private PermisoServiceImpl permisoService;
    private Rol rol;

    @BeforeEach
    void setUp() {
        rol = new Rol("TEST");
        rol.setId(ID_ROL);

        permisoService = new PermisoServiceImpl(
                rolRepository, funcionalidadRepository,
                rolFuncionalidadRepository);
    }

    @Test
    void aplicar_deberiaReemplazarPermisosAtomicamente() {
        Funcionalidad funcAlumnos = mockFuncionalidad(ID_FUNC_ALUMNOS, "ALUMNOS");
        Funcionalidad funcAulas = mockFuncionalidad(ID_FUNC_AULAS, "AULAS");

        when(rolRepository.findById(ID_ROL)).thenReturn(Optional.of(rol));
        when(rolFuncionalidadRepository.findByRolId(ID_ROL)).thenReturn(List.of());
        when(funcionalidadRepository.getReferenceById(ID_FUNC_ALUMNOS)).thenReturn(funcAlumnos);
        when(funcionalidadRepository.getReferenceById(ID_FUNC_AULAS)).thenReturn(funcAulas);

        List<PermisoItem> items = List.of(
                new PermisoItem(ID_FUNC_ALUMNOS, true, true, false, false, true),
                new PermisoItem(ID_FUNC_AULAS, true, false, false, false, false));

        permisoService.aplicar(ID_ROL, items);

        ArgumentCaptor<List<RolFuncionalidad>> captor = ArgumentCaptor.captor();
        verify(rolFuncionalidadRepository).saveAll(captor.capture());

        List<RolFuncionalidad> guardados = captor.getValue();
        assertEquals(2, guardados.size());

        RolFuncionalidad rfAlumnos = encontrarPorFuncionalidad(guardados, ID_FUNC_ALUMNOS);
        assertTrue(rfAlumnos.isVer());
        assertTrue(rfAlumnos.isCrear());
        assertFalse(rfAlumnos.isEditar());
        assertFalse(rfAlumnos.isEliminar());
        assertTrue(rfAlumnos.isImprimir());
        assertTrue(rfAlumnos.isEstado());

        RolFuncionalidad rfAulas = encontrarPorFuncionalidad(guardados, ID_FUNC_AULAS);
        assertTrue(rfAulas.isVer());
        assertFalse(rfAulas.isCrear());
        assertTrue(rfAulas.isEstado());
    }

    @Test
    void aplicar_deberiaActualizarPermisosExistentes() {
        RolFuncionalidad existente = crearRolFuncionalidad(rol, ID_FUNC_ALUMNOS, "ALUMNOS",
                true, false, false, false, false, true);

        when(rolRepository.findById(ID_ROL)).thenReturn(Optional.of(rol));
        when(rolFuncionalidadRepository.findByRolId(ID_ROL)).thenReturn(List.of(existente));

        List<PermisoItem> items = List.of(
                new PermisoItem(ID_FUNC_ALUMNOS, true, true, true, false, false));

        permisoService.aplicar(ID_ROL, items);

        ArgumentCaptor<List<RolFuncionalidad>> captor = ArgumentCaptor.captor();
        verify(rolFuncionalidadRepository).saveAll(captor.capture());

        RolFuncionalidad actualizado = captor.getValue().get(0);
        assertTrue(actualizado.isVer());
        assertTrue(actualizado.isCrear());
        assertTrue(actualizado.isEditar());
        assertFalse(actualizado.isEliminar());
        assertFalse(actualizado.isImprimir());
        assertTrue(actualizado.isEstado());
    }

    @Test
    void aplicar_deberiaReactivarPermisosInactivos() {
        RolFuncionalidad inactivo = crearRolFuncionalidad(rol, ID_FUNC_ALUMNOS, "ALUMNOS",
                true, false, false, false, false, false);

        when(rolRepository.findById(ID_ROL)).thenReturn(Optional.of(rol));
        when(rolFuncionalidadRepository.findByRolId(ID_ROL)).thenReturn(List.of(inactivo));

        List<PermisoItem> items = List.of(
                new PermisoItem(ID_FUNC_ALUMNOS, false, true, false, false, false));

        permisoService.aplicar(ID_ROL, items);

        ArgumentCaptor<List<RolFuncionalidad>> captor = ArgumentCaptor.captor();
        verify(rolFuncionalidadRepository).saveAll(captor.capture());

        RolFuncionalidad reactivado = captor.getValue().get(0);
        assertFalse(reactivado.isVer());
        assertTrue(reactivado.isCrear());
        assertTrue(reactivado.isEstado());
    }

    @Test
    void aplicar_deberiaDesactivarPermisosNoIncluidos() {
        Funcionalidad funcAlumnos = mockFuncionalidad(ID_FUNC_ALUMNOS, "ALUMNOS");
        RolFuncionalidad activo = crearRolFuncionalidad(rol, ID_FUNC_MATRICULA, "MATRICULA",
                true, false, false, false, false, true);

        when(rolRepository.findById(ID_ROL)).thenReturn(Optional.of(rol));
        when(rolFuncionalidadRepository.findByRolId(ID_ROL)).thenReturn(List.of(activo));
        when(funcionalidadRepository.getReferenceById(ID_FUNC_ALUMNOS)).thenReturn(funcAlumnos);

        List<PermisoItem> items = List.of(
                new PermisoItem(ID_FUNC_ALUMNOS, true, false, false, false, false));

        permisoService.aplicar(ID_ROL, items);

        ArgumentCaptor<List<RolFuncionalidad>> captor = ArgumentCaptor.captor();
        verify(rolFuncionalidadRepository).saveAll(captor.capture());

        RolFuncionalidad desactivado = encontrarPorFuncionalidad(captor.getValue(), ID_FUNC_MATRICULA);
        assertFalse(desactivado.isEstado());
    }

    @Test
    void aplicar_deberiaLanzarExcepcionSiRolNoExiste() {
        when(rolRepository.findById(999L)).thenReturn(Optional.empty());

        List<PermisoItem> items = List.of(
                new PermisoItem(ID_FUNC_ALUMNOS, true, false, false, false, false));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> permisoService.aplicar(999L, items));

        assertEquals(ErrorCode.ROL_NO_ENCONTRADO, ex.getErrorCode());
        verify(rolFuncionalidadRepository, never()).saveAll(any());
    }

    @Test
    void obtenerPermisos_deberiaRetornarPermisosActivosDesdeBD() {
        RolFuncionalidad rfAlumnos = crearRolFuncionalidad(rol, ID_FUNC_ALUMNOS, "ALUMNOS",
                true, true, false, false, true, true);
        RolFuncionalidad rfAulas = crearRolFuncionalidad(rol, ID_FUNC_AULAS, "AULAS",
                true, false, false, false, false, true);

        when(rolFuncionalidadRepository.findByRolIdAndEstadoTrue(ID_ROL))
                .thenReturn(List.of(rfAlumnos, rfAulas));

        List<PermisoInfo> resultado = permisoService.obtenerPermisos(ID_ROL);

        assertEquals(2, resultado.size());

        PermisoInfo alumnos = resultado.stream()
                .filter(p -> ID_FUNC_ALUMNOS.equals(p.idFuncionalidad()))
                .findFirst().orElseThrow();
        assertTrue(alumnos.ver());
        assertTrue(alumnos.crear());
        assertTrue(alumnos.imprimir());
        assertEquals("ALUMNOS", alumnos.nombreFuncionalidad());

        PermisoInfo aulas = resultado.stream()
                .filter(p -> ID_FUNC_AULAS.equals(p.idFuncionalidad()))
                .findFirst().orElseThrow();
        assertTrue(aulas.ver());
        assertFalse(aulas.crear());
    }

    @Test
    void obtenerPermisos_deberiaRetornarVacioSiNoHayPermisos() {
        when(rolFuncionalidadRepository.findByRolIdAndEstadoTrue(ID_ROL)).thenReturn(List.of());

        List<PermisoInfo> resultado = permisoService.obtenerPermisos(ID_ROL);

        assertTrue(resultado.isEmpty());
    }

    private Funcionalidad mockFuncionalidad(Long id, String nombre) {
        Funcionalidad f = mock(Funcionalidad.class);
        lenient().when(f.getId()).thenReturn(id);
        lenient().when(f.getNombre()).thenReturn(nombre);
        return f;
    }

    private RolFuncionalidad crearRolFuncionalidad(Rol rol, Long idFunc, String nombreFunc,
                                                    boolean ver, boolean crear, boolean editar,
                                                    boolean eliminar, boolean imprimir,
                                                    boolean estado) {
        Funcionalidad f = mockFuncionalidad(idFunc, nombreFunc);
        RolFuncionalidad rf = new RolFuncionalidad(rol, f, ver, crear, editar, eliminar, imprimir);
        rf.setEstado(estado);
        return rf;
    }

    private RolFuncionalidad encontrarPorFuncionalidad(List<RolFuncionalidad> lista, Long idFunc) {
        return lista.stream()
                .filter(rf -> rf.getFuncionalidad().getId().equals(idFunc))
                .findFirst()
                .orElseThrow(() -> new AssertionError("No se encontro RolFuncionalidad con idFunc=" + idFunc));
    }
}
