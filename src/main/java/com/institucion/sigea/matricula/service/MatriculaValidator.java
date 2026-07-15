package com.institucion.sigea.matricula.service;

import com.institucion.sigea.alumno.entity.Alumno;
import com.institucion.sigea.alumno.repository.AlumnoRepository;
import com.institucion.sigea.aula.entity.AnioAcademico;
import com.institucion.sigea.aula.entity.Aula;
import com.institucion.sigea.aula.repository.AnioAcademicoRepository;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class MatriculaValidator {

    private final AulaRepository aulaRepository;
    private final AlumnoRepository alumnoRepository;
    private final AnioAcademicoRepository anioAcademicoRepository;
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
    public void validar(Aula aulaBloqueada, Long codAlumno, Long codAnioAcademico) {
        validarAlumno(codAlumno);
        validarNoMatriculadoEsteAnio(codAlumno, codAnioAcademico);
        validarVacantesDisponibles(aulaBloqueada, codAnioAcademico);
        validarSinDeudaAnioAnterior(codAlumno, codAnioAcademico);
        validarConceptosActivos(codAnioAcademico);
    }

    public Alumno validarAlumno(Long codAlumno) {
        return alumnoRepository.findById(codAlumno)
                .filter(Alumno::isEstado)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.ALUMNO_NO_ENCONTRADO, "Alumno no encontrado",
                        Map.of("codAlumno", codAlumno)));
    }

    public Aula validarAula(Long codAula) {
        return aulaRepository.findById(codAula)
                .filter(Aula::isEstado)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.AULA_NO_ENCONTRADA, "Aula no encontrada",
                        Map.of("codAula", codAula)));
    }

    public AnioAcademico validarAnioAcademico(Long codAnioAcademico) {
        return anioAcademicoRepository.findById(codAnioAcademico)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.ANIO_NO_ENCONTRADO, "El año académico no existe.",
                        Map.of("codAnioAcademico", codAnioAcademico)));
    }

    public void validarNoMatriculadoEsteAnio(Long codAlumno, Long codAnioAcademico) {
        boolean yaMatriculado = matriculaRepository.existsByCodAlumnoAndCodAnioAcademicoAndEstadoTrue(
                codAlumno.intValue(), codAnioAcademico.intValue());

        if (yaMatriculado) {
            throw new BusinessException(ErrorCode.ALUMNO_DUPLICADO,
                    "El alumno ya se encuentra matriculado en este año académico.",
                    Map.of("codAlumno", codAlumno, "codAnioAcademico", codAnioAcademico));
        }
    }

    public void validarVacantesDisponibles(Aula aula, Long codAnioAcademico) {
        long matriculadosActuales = matriculaRepository.countByCodAulaAndCodAnioAcademicoAndEstadoTrue(
                aula.getId().intValue(), codAnioAcademico.intValue());

        if (matriculadosActuales >= aula.getCapacidadMaxima()) {
            throw new BusinessException(ErrorCode.AULA_SIN_VACANTES,
                    "No existen vacantes disponibles.",
                    Map.of("codAula", aula.getId(), "capacidadMaxima", aula.getCapacidadMaxima()));
        }
    }

    public void validarSinDeudaAnioAnterior(Long codAlumno, Long codAnioAcademico) {
        List<Matricula> matriculasAnteriores = matriculaRepository
                .findByCodAlumnoAndEstadoTrueAndCodAnioAcademicoNot(
                        codAlumno.intValue(), codAnioAcademico.intValue());

        for (Matricula anterior : matriculasAnteriores) {
            long cuotasPendientes = cuotaRepository.countByCodMatriculaAndEstadoCuotaIn(
                    anterior.getId().intValue(),
                    List.of(EstadoCuota.PENDIENTE, EstadoCuota.BLOQUEADA));

            if (cuotasPendientes > 0) {
                throw new BusinessException(ErrorCode.CUOTA_ANTERIOR_PENDIENTE,
                        "El alumno posee deudas anteriores pendientes.",
                        Map.of(
                                "codAlumno", codAlumno,
                                "codMatriculaAnterior", anterior.getId(),
                                "codAnioAcademicoAnterior", anterior.getCodAnioAcademico()));
            }
        }
    }

    public void validarConceptosActivos(Long codAnioAcademico) {
        List<Concepto> conceptosActivos = conceptoRepository.findByAnioAcademicoId(codAnioAcademico).stream()
                .filter(Concepto::isEstado)
                .toList();

        if (conceptosActivos.isEmpty()) {
            throw new BusinessException(ErrorCode.CONCEPTO_NO_ENCONTRADO,
                    "No existen conceptos de pago configurados para el año académico.",
                    Map.of("codAnioAcademico", codAnioAcademico));
        }
    }

    public List<String> validarPreview(Long codAlumno, Long codAula, Long codAnioAcademico) {
        List<String> errores = new ArrayList<>();

        Optional.ofNullable(previewValidarAlumno(codAlumno)).ifPresent(errores::add);
        Optional.ofNullable(previewValidarAula(codAula)).ifPresent(errores::add);
        Optional.ofNullable(previewValidarAnio(codAnioAcademico)).ifPresent(errores::add);

        if (errores.isEmpty()) {
            Optional.ofNullable(previewValidarNoMatriculado(codAlumno, codAnioAcademico)).ifPresent(errores::add);
            Optional.ofNullable(previewValidarVacantes(codAula, codAnioAcademico)).ifPresent(errores::add);
            Optional.ofNullable(previewValidarDeudas(codAlumno, codAnioAcademico)).ifPresent(errores::add);
            Optional.ofNullable(previewValidarConceptos(codAnioAcademico)).ifPresent(errores::add);
        }

        return errores;
    }

    private String previewValidarAlumno(Long codAlumno) {
        Optional<Alumno> opt = alumnoRepository.findById(codAlumno);
        if (opt.isEmpty()) return "El alumno no existe.";
        if (!opt.get().isEstado()) return "El alumno se encuentra inactivo.";
        return null;
    }

    private String previewValidarAula(Long codAula) {
        Optional<Aula> opt = aulaRepository.findById(codAula);
        if (opt.isEmpty()) return "El aula no existe.";
        if (!opt.get().isEstado()) return "El aula no se encuentra activa.";
        return null;
    }

    private String previewValidarAnio(Long codAnioAcademico) {
        Optional<AnioAcademico> opt = anioAcademicoRepository.findById(codAnioAcademico);
        if (opt.isEmpty()) return "El año académico no existe.";
        if (!opt.get().isEstado()) return "El año académico no permite nuevas matrículas.";
        return null;
    }

    private String previewValidarNoMatriculado(Long codAlumno, Long codAnioAcademico) {
        boolean yaMatriculado = matriculaRepository.existsByCodAlumnoAndCodAnioAcademicoAndEstadoTrue(
                codAlumno.intValue(), codAnioAcademico.intValue());
        if (yaMatriculado) return "El alumno ya se encuentra matriculado en este año académico.";
        return null;
    }

    private String previewValidarVacantes(Long codAula, Long codAnioAcademico) {
        Optional<Aula> opt = aulaRepository.findById(codAula);
        if (opt.isEmpty()) return null;
        Aula aula = opt.get();
        long matriculados = matriculaRepository
                .countByCodAulaAndCodAnioAcademicoAndEstadoTrue(
                        aula.getId().intValue(), codAnioAcademico.intValue());
        if (matriculados >= aula.getCapacidadMaxima()) return "No existen vacantes disponibles.";
        return null;
    }

    private String previewValidarDeudas(Long codAlumno, Long codAnioAcademico) {
        List<Matricula> anteriores = matriculaRepository
                .findByCodAlumnoAndEstadoTrueAndCodAnioAcademicoNot(
                        codAlumno.intValue(), codAnioAcademico.intValue());
        for (Matricula anterior : anteriores) {
            long pendientes = cuotaRepository.countByCodMatriculaAndEstadoCuotaIn(
                    anterior.getId().intValue(),
                    List.of(EstadoCuota.PENDIENTE, EstadoCuota.BLOQUEADA));
            if (pendientes > 0) return "El alumno posee deudas anteriores pendientes.";
        }
        return null;
    }

    private String previewValidarConceptos(Long codAnioAcademico) {
        List<Concepto> activos = conceptoRepository.findByAnioAcademicoId(codAnioAcademico).stream()
                .filter(Concepto::isEstado)
                .toList();
        if (activos.isEmpty()) return "No existen conceptos de pago configurados para el año académico.";
        return null;
    }
}