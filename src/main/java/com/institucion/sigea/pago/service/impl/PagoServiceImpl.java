package com.institucion.sigea.pago.service.impl;

import com.institucion.sigea.alumno.entity.Alumno;
import com.institucion.sigea.alumno.repository.AlumnoRepository;
import com.institucion.sigea.concepto.entity.Concepto;
import com.institucion.sigea.concepto.repository.ConceptoRepository;
import com.institucion.sigea.core.exception.BusinessException;
import com.institucion.sigea.core.exception.ErrorCode;
import com.institucion.sigea.matricula.entity.Cuota;
import com.institucion.sigea.matricula.entity.EstadoCuota;
import com.institucion.sigea.matricula.entity.Matricula;
import com.institucion.sigea.matricula.repository.CuotaRepository;
import com.institucion.sigea.matricula.repository.MatriculaRepository;
import com.institucion.sigea.pago.dto.response.CuotaDeudaResponse;
import com.institucion.sigea.pago.dto.response.DeudaAlumnoResponse;
import com.institucion.sigea.pago.dto.response.DeudaHistorialResponse;
import com.institucion.sigea.pago.dto.response.HistorialGeneralResponse;
import com.institucion.sigea.pago.dto.response.PagoDetalleResponse;
import com.institucion.sigea.pago.dto.response.PagoReporteResponse;
import com.institucion.sigea.pago.entity.Pago;
import com.institucion.sigea.pago.mapper.PagoMapper;
import com.institucion.sigea.pago.repository.PagoRepository;
import com.institucion.sigea.pago.service.PagoService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PagoServiceImpl implements PagoService {

    private static final List<EstadoCuota> ESTADOS_DEUDA = List.of(EstadoCuota.PENDIENTE);

    private final CuotaRepository cuotaRepository;
    private final PagoRepository pagoRepository;
    private final ConceptoRepository conceptoRepository;
    private final MatriculaRepository matriculaRepository;
    private final AlumnoRepository alumnoRepository;

    private final PagoMapper pagoMapper;

    @Override
    public List<CuotaDeudaResponse> listarDeudas(Long codAlumno) {
        List<Cuota> cuotas = cuotaRepository.findDeudasPorAlumno(codAlumno.intValue(), ESTADOS_DEUDA);
        return cuotas.stream().map(c -> {
            String nombreConcepto = conceptoRepository.findById(c.getCodConcepto().longValue())
                    .map(Concepto::getNombreConcepto)
                    .orElse(null);
            Integer anioAcademico = matriculaRepository.findById(c.getCodMatricula().longValue())
                    .map(Matricula::getCodAnioAcademico)
                    .orElse(null);
            CuotaDeudaResponse base = pagoMapper.toDeudaResponse(c);
            return new CuotaDeudaResponse(
                    base.codCuota(), base.codMatricula(), base.montoPagar(),
                    base.ordenPago(), base.estadoCuota(), nombreConcepto, anioAcademico);
        }).toList();
    }

    @Override
    public Page<CuotaDeudaResponse> listarCuotasAlumno(Long codAlumno, Pageable pageable) {
        return cuotaRepository.findCuotasPorAlumno(codAlumno.intValue(), pageable)
                .map(c -> {
                    String nombreConcepto = conceptoRepository.findById(c.getCodConcepto().longValue())
                            .map(Concepto::getNombreConcepto)
                            .orElse(null);
                    Integer anioAcademico = matriculaRepository.findById(c.getCodMatricula().longValue())
                            .map(Matricula::getCodAnioAcademico)
                            .orElse(null);
                    CuotaDeudaResponse base = pagoMapper.toDeudaResponse(c);
                    return new CuotaDeudaResponse(
                            base.codCuota(), base.codMatricula(), base.montoPagar(),
                            base.ordenPago(), base.estadoCuota(), nombreConcepto, anioAcademico);
                });
    }

    @Override
    public List<CuotaDeudaResponse> listarTodasCuotasAlumno(Long codAlumno) {
        return cuotaRepository.findTodasCuotasPorAlumno(codAlumno.intValue()).stream()
                .map(c -> {
                    String nombreConcepto = conceptoRepository.findById(c.getCodConcepto().longValue())
                            .map(Concepto::getNombreConcepto)
                            .orElse(null);
                    Integer anioAcademico = matriculaRepository.findById(c.getCodMatricula().longValue())
                            .map(Matricula::getCodAnioAcademico)
                            .orElse(null);
                    CuotaDeudaResponse base = pagoMapper.toDeudaResponse(c);
                    return new CuotaDeudaResponse(
                            base.codCuota(), base.codMatricula(), base.montoPagar(),
                            base.ordenPago(), base.estadoCuota(), nombreConcepto, anioAcademico);
                })
                .toList();
    }

    @Override
    public HistorialGeneralResponse listarHistorialGeneral(Pageable pageable) {
        Page<Cuota> cuotaPage = cuotaRepository.findAllDeudas(ESTADOS_DEUDA, pageable);

        Map<Integer, Matricula> matriculaCache = matriculaRepository.findAllById(
                cuotaPage.getContent().stream()
                        .map(c -> Long.valueOf(c.getCodMatricula()))
                        .collect(Collectors.toList())
        ).stream().collect(Collectors.toMap(m -> m.getId().intValue(), Function.identity()));

        Map<Integer, Alumno> alumnoCache = alumnoRepository.findAllById(
                matriculaCache.values().stream()
                        .map(m -> Long.valueOf(m.getCodAlumno()))
                        .collect(Collectors.toList())
        ).stream().collect(Collectors.toMap(a -> a.getId().intValue(), Function.identity()));

        Map<Integer, Concepto> conceptoCache = conceptoRepository.findAllById(
                cuotaPage.getContent().stream()
                        .map(c -> Long.valueOf(c.getCodConcepto()))
                        .collect(Collectors.toList())
        ).stream().collect(Collectors.toMap(c -> c.getId().intValue(), Function.identity()));

        List<DeudaHistorialResponse> content = cuotaPage.getContent().stream().map(c -> {
            Matricula m = matriculaCache.get(c.getCodMatricula());
            Alumno a = m != null ? alumnoCache.get(m.getCodAlumno()) : null;
            Concepto concepto = conceptoCache.get(c.getCodConcepto());

            String alumnoNombre = a != null
                    ? a.getNombres() + " " + a.getApellidoPaterno()
                    + (a.getApellidoMaterno() != null ? " " + a.getApellidoMaterno() : "")
                    : null;
            String tipoDoc = a != null ? a.getTipoDocumento().getDescripcion() : null;

            return new DeudaHistorialResponse(
                    c.getId(),
                    m != null ? m.getCodAlumno() : null,
                    tipoDoc,
                    a != null ? a.getNumeroDocumento() : null,
                    alumnoNombre,
                    concepto != null ? concepto.getNombreConcepto() : null,
                    m != null ? m.getCodAnioAcademico() : null,
                    c.getMontoPagar(),
                    c.getEstadoCuota().name()
            );
        }).toList();

        long cantidadAlumnos = cuotaRepository.contarAlumnosDeudores(ESTADOS_DEUDA);
        BigDecimal totalDeuda = cuotaRepository.sumarTotalDeuda(ESTADOS_DEUDA);

        return new HistorialGeneralResponse(
                content,
                cuotaPage.getNumber(),
                cuotaPage.getSize(),
                cuotaPage.getTotalElements(),
                cuotaPage.getTotalPages(),
                cantidadAlumnos,
                totalDeuda
        );
    }

    @Override
    public Cuota validarOrdenDePago(Long codCuota) {
        Cuota cuota = cuotaRepository.findWithLockById(codCuota)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.CUOTA_NO_ENCONTRADA, "Cuota no encontrada",
                        Map.of("codCuota", codCuota)));

        if (cuota.getEstadoCuota() == EstadoCuota.PAGADA) {
            throw new BusinessException(ErrorCode.CUOTA_YA_PAGADA, "La cuota ya fue pagada",
                    Map.of("codCuota", codCuota));
        }

        List<Cuota> anterioresPendientes = cuotaRepository
                .findByCodMatriculaAndEstadoCuotaInAndOrdenPagoLessThanOrderByOrdenPagoAsc(
                        cuota.getCodMatricula(), ESTADOS_DEUDA, cuota.getOrdenPago());

        if (!anterioresPendientes.isEmpty()) {
            Concepto concPagado = conceptoRepository.findById(cuota.getCodConcepto().longValue()).orElse(null);
            if (concPagado != null && "OPCIONAL".equals(concPagado.getTipo())) {
                Map<Integer, Concepto> conceptoMap = conceptoRepository.findAllById(
                        anterioresPendientes.stream()
                                .map(c -> Long.valueOf(c.getCodConcepto()))
                                .toList()
                ).stream().collect(Collectors.toMap(c -> c.getId().intValue(), Function.identity()));
                anterioresPendientes = anterioresPendientes.stream()
                        .filter(c -> {
                            Concepto cc = conceptoMap.get(c.getCodConcepto());
                            return cc != null && "FIJO".equals(cc.getTipo());
                        })
                        .toList();
            }
        }

        if (!anterioresPendientes.isEmpty()) {
            Cuota anterior = anterioresPendientes.get(0);
            throw new BusinessException(ErrorCode.CUOTA_ANTERIOR_PENDIENTE,
                    "Existe una cuota anterior pendiente de pago",
                    Map.of("codCuota", codCuota, "codCuotaPendienteAnterior", anterior.getId()));
        }

        return cuota;
    }
    @Override
    public PagoReporteResponse reportarPagos(LocalDateTime desde, LocalDateTime hasta) {
        List<Pago> pagos = pagoRepository.findByFechaPagoBetween(desde, hasta);

        BigDecimal total = pagos.stream()
                .map(Pago::getMontoPagado)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<PagoDetalleResponse> detalle = pagoMapper.toDetalleResponseList(pagos);

        return new PagoReporteResponse(total, pagos.size(), detalle);
    }

    @Override
    public List<DeudaAlumnoResponse> reportarDeudasConsolidadas() {
        return pagoMapper.toDeudaAlumnoResponseList(cuotaRepository.reporteDeudasPorAlumno(ESTADOS_DEUDA));
    }
}
