package com.institucion.sigea.matricula.service.impl;

import com.institucion.sigea.alumno.dto.response.AlumnoMatriculaResponse;
import com.institucion.sigea.alumno.entity.Alumno;
import com.institucion.sigea.alumno.repository.AlumnoRepository;
import com.institucion.sigea.aula.dto.response.AnioAcademicoResponse;
import com.institucion.sigea.aula.dto.response.AulaMatriculaResponse;
import com.institucion.sigea.aula.entity.AnioAcademico;
import com.institucion.sigea.aula.entity.Aula;
import com.institucion.sigea.aula.repository.AnioAcademicoRepository;
import com.institucion.sigea.aula.repository.AulaRepository;
import com.institucion.sigea.auth.service.TotpService;
import com.institucion.sigea.concepto.dto.response.ConceptoResponse;
import com.institucion.sigea.concepto.entity.Concepto;
import com.institucion.sigea.concepto.mapper.ConceptoMapper;
import com.institucion.sigea.concepto.repository.ConceptoRepository;
import com.institucion.sigea.core.exception.BusinessException;
import com.institucion.sigea.core.exception.ErrorCode;
import com.institucion.sigea.matricula.dto.request.MatriculaPreviewRequest;
import com.institucion.sigea.matricula.dto.request.MatriculaRegisterRequest;
import com.institucion.sigea.matricula.dto.response.MatriculaPreviewResponse;
import com.institucion.sigea.matricula.dto.response.MatriculaRegisterResponse;
import com.institucion.sigea.matricula.dto.response.MatriculaRegisterResponse.MatriculaRegistrada;
import com.institucion.sigea.matricula.dto.response.ObligacionResponse;
import com.institucion.sigea.matricula.entity.Cuota;
import com.institucion.sigea.matricula.entity.EstadoCuota;
import com.institucion.sigea.matricula.entity.Matricula;
import com.institucion.sigea.matricula.repository.CuotaRepository;
import com.institucion.sigea.matricula.repository.MatriculaRepository;
import com.institucion.sigea.matricula.service.MatriculaModuleService;
import com.institucion.sigea.matricula.service.MatriculaValidator;
import com.institucion.sigea.security.jwt.JwtPrincipal;
import com.institucion.sigea.usuario.entity.Usuario;
import com.institucion.sigea.usuario.repository.UsuarioRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class MatriculaModuleServiceImpl implements MatriculaModuleService {

    private static final String FORMATO_CODIGO_MATRICULA = "MAT-%d-%04d";

    private final MatriculaValidator matriculaValidator;
    private final AulaRepository aulaRepository;
    private final AnioAcademicoRepository anioAcademicoRepository;
    private final AlumnoRepository alumnoRepository;
    private final ConceptoRepository conceptoRepository;
    private final ConceptoMapper conceptoMapper;
    private final MatriculaRepository matriculaRepository;
    private final CuotaRepository cuotaRepository;
    private final UsuarioRepository usuarioRepository;
    private final TotpService totpService;
    private final CacheManager cacheManager;
    private final EntityManager entityManager;

    @Override
    @Transactional(readOnly = true)
    public MatriculaPreviewResponse preview(MatriculaPreviewRequest request) {
        JwtPrincipal principal = (JwtPrincipal) SecurityContextHolder
                .getContext().getAuthentication().getPrincipal();
        Usuario usuario = usuarioRepository.findById(principal.userId())
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.INVALID_CREDENTIALS, "Usuario no encontrado"));
        boolean totpVerificado = usuario.isTotpVerificado();

        List<String> errores = matriculaValidator.validarPreview(
                request.alumnoId(), request.aulaId(), request.anioId());

        if (!errores.isEmpty()) {
            return MatriculaPreviewResponse.invalido(errores);
        }

        Alumno alumno = alumnoRepository.findById(request.alumnoId()).orElseThrow();
        Aula aula = aulaRepository.findById(request.aulaId()).orElseThrow();
        AnioAcademico anio = anioAcademicoRepository.findById(request.anioId()).orElseThrow();

        AlumnoMatriculaResponse alumnoResp = new AlumnoMatriculaResponse(
                alumno.getId(), alumno.getNumeroDocumento(),
                alumno.getApellidoPaterno(), alumno.getApellidoMaterno(),
                alumno.getNombres(), alumno.isEstado());

        long matriculados = matriculaRepository
                .countByCodAulaAndCodAnioAcademicoAndEstadoTrue(
                        aula.getId().intValue(), anio.getAnio());

        AulaMatriculaResponse aulaResp = new AulaMatriculaResponse(
                aula.getId(), aula.getNivel().getNombre(),
                aula.getGrado().getNombreGrado(), aula.getSeccion(),
                matriculados, aula.getCapacidadMaxima(),
                aula.isEstado(), anio.getAnio());

        AnioAcademicoResponse anioResp = new AnioAcademicoResponse(
                anio.getId(), anio.getAnio(), anio.isEstado());

        List<Concepto> conceptos = conceptoRepository
                .findByAnioAcademicoId(request.anioId()).stream()
                .filter(Concepto::isEstado)
                .toList();

        List<ConceptoResponse> conceptosResp = conceptos.stream()
                .map(conceptoMapper::toResponse)
                .toList();

        BigDecimal total = conceptos.stream()
                .map(Concepto::getMonto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long capacidad = aula.getCapacidadMaxima();
        long vacantes = capacidad - matriculados;

        return MatriculaPreviewResponse.valido(
                alumnoResp, aulaResp, anioResp,
                conceptosResp, total, capacidad, matriculados, vacantes, totpVerificado);
    }

    @Override
    @Transactional
    public MatriculaRegisterResponse register(MatriculaRegisterRequest request) {
        JwtPrincipal principal = (JwtPrincipal) SecurityContextHolder
                .getContext().getAuthentication().getPrincipal();

        Usuario usuario = usuarioRepository.findById(principal.userId())
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.INVALID_CREDENTIALS, "Usuario no encontrado"));

        Aula aula = aulaRepository.findWithLockById(request.aulaId())
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.AULA_NO_ENCONTRADA, "Aula no encontrada",
                        Map.of("codAula", request.aulaId())));

        AnioAcademico anio = anioAcademicoRepository.findById(request.anioId())
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.ANIO_NO_ENCONTRADO, "El año académico no existe."));

        if (!anio.isEstado()) {
            throw new BusinessException(ErrorCode.ANIO_CERRADO,
                    "El año académico no permite nuevas matrículas.");
        }

        matriculaValidator.validar(aula, request.alumnoId(), request.anioId());

        verificarTotp(usuario, request.codigoTotp());
        usuario.setTotpVerificado(true);

        Matricula matricula = new Matricula();
        matricula.setCodAlumno(request.alumnoId().intValue());
        matricula.setCodAula(request.aulaId().intValue());
        matricula.setCodAnioAcademico(request.anioId().intValue());
        matricula.setFechaMatricula(LocalDateTime.now());
        matricula.setEstadoMatricula("ACTIVO");
        matricula.setCodUsuario(usuario.getId().intValue());

        int anioActual = Year.now().getValue();
        Long siguienteCorrelativo = ((Number) entityManager
                .createNativeQuery("SELECT nextval('seq_matricula')")
                .getSingleResult()).longValue();
        matricula.setCodigo(FORMATO_CODIGO_MATRICULA.formatted(anioActual, siguienteCorrelativo));
        matriculaRepository.save(matricula);

        List<Concepto> conceptosActivos = conceptoRepository
                .findByAnioAcademicoId(request.anioId()).stream()
                .filter(Concepto::isEstado)
                .toList();

        Set<Long> idsSeleccionados = Set.copyOf(request.conceptosActivos());

        List<Concepto> seleccionados = conceptosActivos.stream()
                .filter(c -> idsSeleccionados.contains(c.getId()))
                .sorted(Comparator.comparing(Concepto::getOrdenPago))
                .toList();

        List<Concepto> obligatorios = conceptosActivos.stream()
                .filter(Concepto::isObligatorio)
                .toList();
        for (Concepto obligatorio : obligatorios) {
            if (!idsSeleccionados.contains(obligatorio.getId())) {
                throw new BusinessException(ErrorCode.VALIDACION_FORMULARIO,
                        "El concepto obligatorio " + obligatorio.getNombreConcepto()
                        + " debe estar seleccionado.",
                        Map.of("conceptoId", obligatorio.getId(),
                                "nombreConcepto", obligatorio.getNombreConcepto()));
            }
        }

        List<Cuota> cuotas = IntStream.range(0, seleccionados.size())
                .mapToObj(i -> {
                    Concepto concepto = seleccionados.get(i);
                    Cuota cuota = new Cuota();
                    cuota.setCodMatricula(matricula.getId().intValue());
                    cuota.setCodConcepto(concepto.getId().intValue());
                    cuota.setMontoPagar(concepto.getMonto());
                    cuota.setOrdenPago(concepto.getOrdenPago());
                    cuota.setEstadoCuota(i == 0 ? EstadoCuota.PENDIENTE : EstadoCuota.BLOQUEADA);
                    cuota.setSaldoPendiente(concepto.getMonto());
                    cuota.setFechaVencimiento(LocalDate.of(anio.getAnio() + 1, 3, 15));
                    return cuota;
                })
                .toList();
        cuotaRepository.saveAll(cuotas);

        MatriculaRegistrada matriculaReg = new MatriculaRegistrada(
                matricula.getId(),
                matricula.getCodAlumno().longValue(),
                matricula.getCodAula().longValue(),
                matricula.getCodAnioAcademico().longValue(),
                matricula.getFechaMatricula().toLocalDate(),
                matricula.getEstadoMatricula(),
                matricula.getCodUsuario().longValue());

        Map<Integer, String> nombreConceptos = conceptosActivos.stream()
                .collect(Collectors.toMap(
                        c -> c.getId().intValue(),
                        Concepto::getNombreConcepto));

        List<ObligacionResponse> obligaciones = cuotas.stream()
                .map(c -> new ObligacionResponse(
                        c.getId(),
                        (long) c.getCodConcepto(),
                        nombreConceptos.getOrDefault(c.getCodConcepto(), "Concepto"),
                        c.getMontoPagar(),
                        c.getEstadoCuota().name(),
                        c.getFechaVencimiento(),
                        c.getOrdenPago(),
                        c.getSaldoPendiente()))
                .toList();

        return MatriculaRegisterResponse.exito(matriculaReg, obligaciones);
    }

    private void verificarTotp(Usuario usuario, String codigoTotp) {
        if (codigoTotp == null || codigoTotp.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_OTP,
                    "Código TOTP obligatorio para matricularse.");
        }

        Cache cache = cacheManager.getCache(
                com.institucion.sigea.config.CacheConfig.CACHE_OTP_INTENTOS_MATRICULA);
        Long userId = usuario.getId();
        String cacheKey = "intentos:" + userId;

        Integer intentos = cache.get(cacheKey, Integer.class);
        if (intentos != null && intentos >= 3) {
            throw new BusinessException(ErrorCode.OTP_BLOCKED,
                    "Demasiados intentos fallidos de OTP. Espere 5 minutos.");
        }

        try {
            totpService.verificarCodigo(usuario.getTotpSecret(), codigoTotp);
            if (intentos != null) {
                cache.evict(cacheKey);
            }
        } catch (Exception e) {
            int nuevoIntento = (intentos != null ? intentos : 0) + 1;
            cache.put(cacheKey, nuevoIntento);
            throw new BusinessException(ErrorCode.INVALID_OTP,
                    "Código OTP inválido. Intento " + nuevoIntento + " de 3.");
        }
    }
}