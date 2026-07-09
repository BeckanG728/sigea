package com.institucion.sigea.pago;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.institucion.sigea.matricula.entity.Cuota;
import com.institucion.sigea.matricula.entity.EstadoCuota;
import com.institucion.sigea.matricula.entity.Matricula;
import com.institucion.sigea.matricula.repository.CuotaRepository;
import com.institucion.sigea.matricula.repository.MatriculaRepository;
import com.institucion.sigea.pago.dto.request.RegistrarPagoRequest;
import com.institucion.sigea.pago.entity.MedioPago;
import com.institucion.sigea.pago.entity.Recibo;
import com.institucion.sigea.pago.repository.PagoRepository;
import com.institucion.sigea.pago.repository.ReciboRepository;
import com.institucion.sigea.pago.service.PagoTransaccionService;
import com.institucion.sigea.security.jwt.JwtUtil;
import com.institucion.sigea.usuario.entity.Rol;
import com.institucion.sigea.usuario.entity.Usuario;
import com.institucion.sigea.usuario.repository.RolRepository;
import com.institucion.sigea.usuario.repository.UsuarioRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Year;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PagoIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JwtUtil jwtUtil;
    @Autowired private PagoTransaccionService pagoTransaccionService;

    @Autowired private RolRepository rolRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private MatriculaRepository matriculaRepository;
    @Autowired private CuotaRepository cuotaRepository;
    @Autowired private PagoRepository pagoRepository;
    @Autowired private ReciboRepository reciboRepository;

    private String token;
    private Matricula matricula;
    private Cuota cuota1; // ordenPago 1 — la que se debe pagar primero
    private Cuota cuota2; // ordenPago 2

    @BeforeEach
    void setUp() {
        Rol rol = rolRepository.findByNombreRol("SUPERUSUARIO")
                .orElseGet(() -> rolRepository.save(new Rol("SUPERUSUARIO")));

        Usuario usuario = new Usuario();
        usuario.setNombreUsuario("secretaria.pagos");
        usuario.setPassword(passwordEncoder.encode("Clave123!"));
        usuario.setRol(rol);
        usuario.setDosFactorHabilitado(false);
        usuario = usuarioRepository.save(usuario);

        token = jwtUtil.generateToken(usuario.getId(), usuario.getNombreUsuario(), "SUPERUSUARIO", true);

        matricula = new Matricula();
        matricula.setCodAlumno(1);
        matricula.setCodAula(1);
        matricula.setCodAnioAcademico(2026);
        matricula.setFechaMatricula(LocalDateTime.now());
        matricula = matriculaRepository.save(matricula);

        cuota1 = nuevaCuota(matricula, (short) 1, new BigDecimal("50.00"));
        cuota2 = nuevaCuota(matricula, (short) 2, new BigDecimal("80.00"));
        cuota1 = cuotaRepository.save(cuota1);
        cuota2 = cuotaRepository.save(cuota2);
    }

    @AfterEach
    void tearDown() {
        reciboRepository.deleteAll();
        pagoRepository.deleteAll();
        cuotaRepository.deleteAll();
        matriculaRepository.deleteAll();
        usuarioRepository.deleteAll();
        rolRepository.deleteAll();
    }

    @Test
    void pago_fuera_de_orden_esRechazado() throws Exception {
        RegistrarPagoRequest request = new RegistrarPagoRequest(
                cuota2.getId(), cuota2.getMontoPagar(), MedioPago.EFECTIVO);

        mockMvc.perform(post("/pagos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("CUOTA_ANTERIOR_PENDIENTE"));

        assertEquals(0, pagoRepository.count());
        assertEquals(0, reciboRepository.count());
        assertEquals(EstadoCuota.PENDIENTE, cuotaRepository.findById(cuota1.getId()).orElseThrow().getEstadoCuota());
    }

    @Test
    void pago_exitoso_generaReciboYMarcaCuotaPagada() throws Exception {
        RegistrarPagoRequest request = new RegistrarPagoRequest(
                cuota1.getId(), cuota1.getMontoPagar(), MedioPago.EFECTIVO);

        String numeroReciboEsperado = "R-%d-00001".formatted(Year.now().getValue());

        mockMvc.perform(post("/pagos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.numeroRecibo").value(numeroReciboEsperado));

        assertEquals(1, pagoRepository.count());
        assertEquals(1, reciboRepository.count());
        assertEquals(EstadoCuota.PAGADA, cuotaRepository.findById(cuota1.getId()).orElseThrow().getEstadoCuota());
    }

    @Test
    void pago_rollback_anteFalloDeCorrelativo() {

        Recibo reciboExistente = new Recibo();
        reciboExistente.setNumeroRecibo("R-%d-00001".formatted(Year.now().getValue()));
        reciboExistente.setCodPago(999);
        reciboExistente.setAnio(Year.now().getValue());
        reciboExistente.setCorrelativo(1);
        reciboExistente.setFechaEmision(LocalDateTime.now());
        reciboRepository.save(reciboExistente);

        RegistrarPagoRequest request = new RegistrarPagoRequest(
                cuota1.getId(), cuota1.getMontoPagar(), MedioPago.EFECTIVO);

        assertThrows(RuntimeException.class, () -> pagoTransaccionService.registrarPago(request));

        assertEquals(0, pagoRepository.count());
        assertEquals(1, reciboRepository.count()); // solo el que insertamos como precondición
        assertEquals(EstadoCuota.PENDIENTE, cuotaRepository.findById(cuota1.getId()).orElseThrow().getEstadoCuota());
    }

    private Cuota nuevaCuota(Matricula matricula, short ordenPago, BigDecimal monto) {
        Cuota cuota = new Cuota();
        cuota.setCodMatricula(matricula.getId().intValue());
        cuota.setCodConcepto(1);
        cuota.setMontoPagar(monto);
        cuota.setOrdenPago(ordenPago);
        cuota.setEstadoCuota(EstadoCuota.PENDIENTE);
        return cuota;
    }
}
