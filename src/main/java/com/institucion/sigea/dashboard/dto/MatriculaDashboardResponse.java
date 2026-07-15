package com.institucion.sigea.dashboard.dto;

import com.institucion.sigea.aula.entity.Aula;
import com.institucion.sigea.matricula.entity.Matricula;
import com.institucion.sigea.usuario.entity.Usuario;
import com.institucion.sigea.alumno.entity.Alumno;

import java.time.format.DateTimeFormatter;

public record MatriculaDashboardResponse(
        long n,
        String alumno,
        String aula,
        String fecha,
        String estado,
        String registradoPor
) {
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public static MatriculaDashboardResponse from(Matricula m, Alumno al, Aula au, Usuario u, long index) {
        String nombreAlumno = al.getApellidoPaterno() + " " + al.getApellidoMaterno() + ", " + al.getNombres();
        String descAula = au.getNivel().getNombre() + " " + au.getGrado().getNombreGrado() + " " + au.getSeccion();
        String nombreUsuario = u.getNombre() + " " + u.getPrimerApellido();
        return new MatriculaDashboardResponse(
                index,
                nombreAlumno,
                descAula,
                m.getFechaMatricula().format(FMT),
                m.getEstadoMatricula(),
                nombreUsuario
        );
    }
}
