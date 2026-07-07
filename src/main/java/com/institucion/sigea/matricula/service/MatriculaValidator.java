package com.institucion.sigea.matricula.service;

import com.institucion.sigea.alumno.entity.Alumno;
import com.institucion.sigea.alumno.repository.AlumnoRepository;
import com.institucion.sigea.aula.entity.Aula;
import com.institucion.sigea.aula.repository.AulaRepository;
import com.institucion.sigea.concepto.entity.Concepto;
import com.institucion.sigea.concepto.repository.ConceptoRepository;
import com.institucion.sigea.core.exception.BusinessException;
import com.institucion.sigea.core.exception.ErrorCode;
import com.institucion.sigea.matricula.entity.EstadoCuota;
import com.institucion.sigea.matricula.entity.Matricula;
import com.institucion.sigea.matricula.repository.CuotaRepository;
import com.institucion.sigea.matricula.repository.MatriculaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class MatriculaValidator {

    private final AulaRepository aulaRepository;
    private final AlumnoRepository alumnoRepository;
    private final ConceptoRepository conceptoRepository;
    private final MatriculaRepository matriculaRepository;
    private final CuotaRepository cuotaRepository;

    public void validar(Long codAlumno, Long codAula, Long codAnioAcademico) {
        Aula aula = validarAula(codAula);
        validarAlumno(codAlumno);
        validarNoMatriculadoEsteAnio(codAlumno, codAnioAcademico);
        validarVacantesDisponibles(aula, codAnioAcademico);
        validarSinDeudaAnioAnterior(codAlumno, codAnioAcademico);
        validarConceptosActivos(codAnioAcademico);
    }

    private Aula validarAula(Long codAula) {
        return aulaRepository.findById(codAula)
                .filter(Aula::isEstado)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.AULA_NO_ENCONTRADA, "Aula no encontrada",
                        Map.of("codAula", codAula)));
    }

    private void validarAlumno(Long codAlumno) {
        alumnoRepository.findById(codAlumno)
                .filter(Alumno::isEstado)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.ALUMNO_NO_ENCONTRADO, "Alumno no encontrado",
                        Map.of("codAlumno", codAlumno)));
    }

    private void validarNoMatriculadoEsteAnio(Long codAlumno, Long codAnioAcademico) {
        boolean yaMatriculado = matriculaRepository.existsByCodAlumnoAndCodAnioAcademicoAndEstadoTrue(
                codAlumno.intValue(), codAnioAcademico.intValue());

        if (yaMatriculado) {
            throw new BusinessException(ErrorCode.ALUMNO_DUPLICADO,
                    "El alumno ya está matriculado en este año académico",
                    Map.of("codAlumno", codAlumno, "codAnioAcademico", codAnioAcademico));
        }
    }

    private void validarVacantesDisponibles(Aula aula, Long codAnioAcademico) {
        long matriculadosActuales = matriculaRepository.countByCodAulaAndCodAnioAcademicoAndEstadoTrue(
                aula.getId().intValue(), codAnioAcademico.intValue());

        if (matriculadosActuales >= aula.getCapacidadMaxima()) {
            throw new BusinessException(ErrorCode.AULA_SIN_VACANTES,
                    "El aula no tiene vacantes disponibles",
                    Map.of("codAula", aula.getId(), "capacidadMaxima", aula.getCapacidadMaxima()));
        }
    }

    private void validarSinDeudaAnioAnterior(Long codAlumno, Long codAnioAcademico) {
        List<Matricula> matriculasAnteriores = matriculaRepository
                .findByCodAlumnoAndEstadoTrueAndCodAnioAcademicoNot(
                        codAlumno.intValue(), codAnioAcademico.intValue());

        for (Matricula anterior : matriculasAnteriores) {
            long cuotasPendientes = cuotaRepository.countByCodMatriculaAndEstadoCuotaIn(
                    anterior.getId().intValue(),
                    List.of(EstadoCuota.PENDIENTE, EstadoCuota.BLOQUEADA));

            if (cuotasPendientes > 0) {
                throw new BusinessException(ErrorCode.CUOTA_ANTERIOR_PENDIENTE,
                        "El alumno tiene cuotas pendientes de un año académico anterior",
                        Map.of(
                                "codAlumno", codAlumno,
                                "codMatriculaAnterior", anterior.getId(),
                                "codAnioAcademicoAnterior", anterior.getCodAnioAcademico()));
            }
        }
    }

    private void validarConceptosActivos(Long codAnioAcademico) {
        List<Concepto> conceptosActivos = conceptoRepository.findByAnioAcademicoId(codAnioAcademico).stream()
                .filter(Concepto::isEstado)
                .toList();

        if (conceptosActivos.isEmpty()) {
            throw new BusinessException(ErrorCode.CONCEPTO_NO_ENCONTRADO,
                    "No existen conceptos de pago activos para este año académico",
                    Map.of("codAnioAcademico", codAnioAcademico));
        }
    }
}