package com.institucion.sigea.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.institucion.sigea.alumno.entity.Alumno;
import com.institucion.sigea.alumno.entity.TipoDocumento;
import com.institucion.sigea.alumno.repository.AlumnoRepository;
import com.institucion.sigea.alumno.repository.TipoDocumentoRepository;
import com.institucion.sigea.aula.entity.*;
import com.institucion.sigea.aula.repository.*;
import com.institucion.sigea.auth.dto.request.LoginRequest;
import com.institucion.sigea.auth.dto.request.Verify2faRequest;
import com.institucion.sigea.concepto.entity.Concepto;
import com.institucion.sigea.concepto.entity.TipoConcepto;
import com.institucion.sigea.concepto.repository.ConceptoRepository;
import com.institucion.sigea.concepto.repository.TipoConceptoRepository;
import com.institucion.sigea.matricula.dto.request.MatriculaRequest;
import com.institucion.sigea.matricula.repository.CuotaRepository;
import com.institucion.sigea.matricula.repository.MatriculaRepository;
import com.institucion.sigea.pago.dto.request.RegistrarPagoRequest;
import com.institucion.sigea.pago.entity.MedioPago;
import com.institucion.sigea.pago.repository.PagoRepository;
import com.institucion.sigea.pago.repository.ReciboRepository;
import com.institucion.sigea.usuario.entity.Rol;
import com.institucion.sigea.usuario.entity.Usuario;
import com.institucion.sigea.usuario.repository.RolRepository;
import com.institucion.sigea.usuario.repository.UsuarioRepository;
import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class EndToEndIntegrationTest {

    private static final String PASSWORD = "Clave123!";
    private static final long PERIODO_TOTP_SEGUNDOS = 30;

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private PasswordEncoder passwordEncoder;

    @Autowired private RolRepository rolRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private AnioAcademicoRepository anioAcademicoRepository;
    @Autowired private NivelRepository nivelRepository;
    @Autowired private GradoRepository gradoRepository;
    @Autowired private AulaRepository aulaRepository;
    @Autowired private TipoDocumentoRepository tipoDocumentoRepository;
    @Autowired private AlumnoRepository alumnoRepository;
    @Autowired private TipoConceptoRepository tipoConceptoRepository;
    @Autowired private ConceptoRepository conceptoRepository;
    @Autowired private MatriculaRepository matriculaRepository;
    @Autowired private CuotaRepository cuotaRepository;
    @Autowired private PagoRepository pagoRepository;
    @Autowired private ReciboRepository reciboRepository;

    private final DefaultSecretGenerator secretGenerator = new DefaultSecretGenerator();
    private final DefaultCodeGenerator codeGenerator = new DefaultCodeGenerator();
    private final SystemTimeProvider timeProvider = new SystemTimeProvider();

    private Usuario usuario;
    private String secretoTotp;

    @BeforeEach
    void setUp() {
        Rol rol = rolRepository.findByNombre("SUPERUSUARIO")
                .orElseGet(() -> rolRepository.save(new Rol("SUPERUSUARIO")));

        secretoTotp = secretGenerator.generate();

        usuario = new Usuario();
        usuario.setNombreUsuario("secretaria.e2e");
        usuario.setPassword(passwordEncoder.encode(PASSWORD));
        usuario.setRol(rol);
        usuario.setLogin2fa(true);
        usuario.setTotpSecret(secretoTotp); // AesConverter cifra al guardar
        usuario = usuarioRepository.save(usuario);
    }

    @AfterEach
    void tearDown() {
        reciboRepository.deleteAll();
        pagoRepository.deleteAll();
        cuotaRepository.deleteAll();
        matriculaRepository.deleteAll();
        conceptoRepository.deleteAll();
        tipoConceptoRepository.deleteAll();
        alumnoRepository.deleteAll();
        tipoDocumentoRepository.deleteAll();
        aulaRepository.deleteAll();
        gradoRepository.deleteAll();
        nivelRepository.deleteAll();
        anioAcademicoRepository.deleteAll();
        usuarioRepository.deleteAll();
        rolRepository.deleteAll();
    }

    @Test
    void login_dos_pasos_exitoso_y_flujo_completo() throws Exception {
        // Paso 1: login con usuario/contraseña -> pide 2FA
        String loginJson = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest("secretaria.e2e", PASSWORD))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.requiere2FA").value(true))
                .andExpect(jsonPath("$.token").doesNotExist())
                .andReturn().getResponse().getContentAsString();

        Long idUsuario = objectMapper.readTree(loginJson).get("idUsuario").asLong();

        long counter = Math.floorDiv(timeProvider.getTime(), PERIODO_TOTP_SEGUNDOS);
        String codigoTotp = codeGenerator.generate(secretoTotp, counter);

        String verifyJson = mockMvc.perform(post("/auth/login/verify-2fa")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new Verify2faRequest(idUsuario, codigoTotp))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.requiere2FA").value(false))
                .andReturn().getResponse().getContentAsString();

        String token = objectMapper.readTree(verifyJson).get("token").asText();

        // --- A partir de aquí, el resto del flujo con el token REAL emitido ---
        AnioAcademico anio = anioAcademicoRepository.save(nuevoAnio(2026));
        Nivel nivel = nivelRepository.save(nuevoNivel("Primaria"));
        Grado grado = gradoRepository.save(nuevoGrado(nivel, "1°"));

        Aula aula = new Aula();
        aula.setAnioAcademico(anio);
        aula.setNivel(nivel);
        aula.setGrado(grado);
        aula.setSeccion("A");
        aula.setCapacidadMaxima((short) 10);
        aula = aulaRepository.save(aula);

        TipoDocumento dni = tipoDocumentoRepository.save(nuevoTipoDocumento("DNI"));
        Alumno alumno = alumnoRepository.save(nuevoAlumno(dni, "11223344"));

        TipoConcepto tipoConcepto = tipoConceptoRepository.save(nuevoTipoConcepto("Matrícula"));
        Concepto concepto = new Concepto();
        concepto.setAnioAcademico(anio);
        concepto.setTipoConcepto(tipoConcepto);
        concepto.setNombreConcepto("Matrícula 2026");
        concepto.setMonto(new BigDecimal("120.00"));
        concepto.setOrdenPago((short) 1);
        concepto.setObligatorio(true);
        conceptoRepository.save(concepto);

        // Matricular
        String matriculaJson = mockMvc.perform(post("/matriculas")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new MatriculaRequest(alumno.getId(), aula.getId(), anio.getId(), null))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long codCuota = objectMapper.readTree(matriculaJson).get("cuotas").get(0).get("id").asLong();

        // Pagar
        mockMvc.perform(post("/pagos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new RegistrarPagoRequest(codCuota, new BigDecimal("120.00"), MedioPago.EFECTIVO))))
                .andExpect(status().isCreated());

        // Verificar que la auditoría registró el login y las operaciones de negocio
        mockMvc.perform(get("/reportes/auditoria")
                        .header("Authorization", "Bearer " + token)
                        .param("codUsuario", String.valueOf(idUsuario)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.operacion == 'LOGIN')]").exists());
    }

    @Test
    void login_bloqueado_por_intentos() throws Exception {
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    new LoginRequest("secretaria.e2e", "ClaveIncorrecta"))))
                    .andExpect(status().isUnauthorized());
        }

        // 6to intento, incluso con la contraseña correcta, debe quedar bloqueado
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest("secretaria.e2e", PASSWORD))))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.error").value("LOGIN_BLOCKED"));
    }

    // ---- helpers de seeding (mismos que en MatriculaIntegrationTest) ----

    private AnioAcademico nuevoAnio(int anio) {
        AnioAcademico a = new AnioAcademico();
        a.setAnio(anio);
        return a;
    }

    private Nivel nuevoNivel(String nombre) {
        Nivel n = new Nivel();
        n.setNombre(nombre);
        return n;
    }

    private Grado nuevoGrado(Nivel nivel, String nombreGrado) {
        Grado g = new Grado();
        g.setNivel(nivel);
        g.setNombreGrado(nombreGrado);
        return g;
    }

    private TipoDocumento nuevoTipoDocumento(String descripcion) {
        TipoDocumento t = new TipoDocumento();
        t.setDescripcion(descripcion);
        return t;
    }

    private TipoConcepto nuevoTipoConcepto(String nombre) {
        TipoConcepto t = new TipoConcepto();
        t.setNombre(nombre);
        return t;
    }

    private Alumno nuevoAlumno(TipoDocumento tipoDocumento, String numeroDocumento) {
        Alumno alumno = new Alumno();
        alumno.setTipoDocumento(tipoDocumento);
        alumno.setNumeroDocumento(numeroDocumento);
        alumno.setNombres("Alumno");
        alumno.setApellidoPaterno("E2E");
        alumno.setApellidoMaterno("Test");
        alumno.setFechaNacimiento("2015-01-01");
        return alumno;
    }
}