package com.institucion.sigea.matricula;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.institucion.sigea.alumno.entity.Alumno;
import com.institucion.sigea.alumno.entity.TipoDocumento;
import com.institucion.sigea.alumno.repository.AlumnoRepository;
import com.institucion.sigea.alumno.repository.TipoDocumentoRepository;
import com.institucion.sigea.aula.entity.*;
import com.institucion.sigea.aula.repository.*;
import com.institucion.sigea.concepto.entity.Concepto;
import com.institucion.sigea.concepto.entity.TipoConcepto;
import com.institucion.sigea.concepto.repository.ConceptoRepository;
import com.institucion.sigea.concepto.repository.TipoConceptoRepository;
import com.institucion.sigea.matricula.dto.request.MatriculaRequest;
import com.institucion.sigea.matricula.repository.CuotaRepository;
import com.institucion.sigea.matricula.repository.MatriculaRepository;
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
import org.hamcrest.Matchers.*;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MatriculaIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JwtUtil jwtUtil;

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

    private AnioAcademico anio;
    private Aula aula; // capacidadMaxima = 1, a propósito, para forzar AULA_SIN_VACANTES
    private Alumno alumno1;
    private Alumno alumno2;
    private String tokenCon2fa;
    private String tokenSin2fa;

    @BeforeEach
    void setUp() {
        Rol rol = rolRepository.findByNombreRol("SUPERUSUARIO")
                .orElseGet(() -> rolRepository.save(new Rol("SUPERUSUARIO")));

        Usuario usuario = new Usuario();
        usuario.setUsername("secretaria.test");
        usuario.setPassword(passwordEncoder.encode("Clave123!"));
        usuario.setRol(rol);
        usuario.setDosFactorHabilitado(false);
        usuario = usuarioRepository.save(usuario);

        tokenCon2fa = jwtUtil.generateToken(usuario.getId(), usuario.getUsername(), "SUPERUSUARIO", true);
        tokenSin2fa = jwtUtil.generateToken(usuario.getId(), usuario.getUsername(), "SUPERUSUARIO", false);

        anio = anioAcademicoRepository.save(nuevoAnio(2026));
        Nivel nivel = nivelRepository.save(nuevoNivel("Primaria"));
        Grado grado = gradoRepository.save(nuevoGrado(nivel, "1°"));

        aula = new Aula();
        aula.setAnioAcademico(anio);
        aula.setNivel(nivel);
        aula.setGrado(grado);
        aula.setSeccion("A");
        aula.setCapacidadMaxima((short) 1);
        aula = aulaRepository.save(aula);

        TipoDocumento dni = tipoDocumentoRepository.save(nuevoTipoDocumento("DNI"));
        alumno1 = alumnoRepository.save(nuevoAlumno(dni, "12345678"));
        alumno2 = alumnoRepository.save(nuevoAlumno(dni, "87654321"));

        TipoConcepto tipoConcepto = tipoConceptoRepository.save(nuevoTipoConcepto("Matrícula"));
        Concepto concepto = new Concepto();
        concepto.setAnioAcademico(anio);
        concepto.setTipoConcepto(tipoConcepto);
        concepto.setNombreConcepto("Matrícula 2026");
        concepto.setMonto(new BigDecimal("100.00"));
        concepto.setOrdenPago((short) 1);
        concepto.setObligatorio(true);
        conceptoRepository.save(concepto);
    }

    @AfterEach
    void tearDown() {
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
    void matricula_exitosa() throws Exception {
        MatriculaRequest request = new MatriculaRequest(alumno1.getId(), aula.getId(), anio.getId());

        mockMvc.perform(post("/matriculas")
                        .header("Authorization", "Bearer " + tokenCon2fa)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.cuotas", hasSize(1)))
                .andExpect(jsonPath("$.cuotas[0].estadoCuota").value("PENDIENTE"));

        assertEquals(1, matriculaRepository.count());
        assertEquals(1, cuotaRepository.count());
    }

    @Test
    void matricula_sin_2fa_rechazada() throws Exception {
        MatriculaRequest request = new MatriculaRequest(alumno1.getId(), aula.getId(), anio.getId());

        mockMvc.perform(post("/matriculas")
                        .header("Authorization", "Bearer " + tokenSin2fa)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("TWOFA_REQUIRED"));

        assertEquals(0, matriculaRepository.count());
    }

    @Test
    void matricula_rollback_aulaSinVacantes() throws Exception {
        // Ocupa la única vacante del aula con alumno1
        mockMvc.perform(post("/matriculas")
                        .header("Authorization", "Bearer " + tokenCon2fa)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new MatriculaRequest(alumno1.getId(), aula.getId(), anio.getId()))))
                .andExpect(status().isCreated());

        // alumno2 intenta matricularse en la misma aula, ya sin vacantes
        mockMvc.perform(post("/matriculas")
                        .header("Authorization", "Bearer " + tokenCon2fa)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new MatriculaRequest(alumno2.getId(), aula.getId(), anio.getId()))))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error").value("AULA_SIN_VACANTES"));

        // Solo debe existir la matrícula (y su cuota) de alumno1 — el intento
        // de alumno2 no dejó ninguna fila huérfana.
        assertEquals(1, matriculaRepository.count());
        assertEquals(1, cuotaRepository.count());
    }

    // ---- helpers de seeding ----

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
        alumno.setNumeroDocumento(numeroDocumento); // AesConverter cifra/descifra transparente
        alumno.setNombres("Alumno");
        alumno.setApellidoPaterno("De Prueba");
        alumno.setApellidoMaterno("Test");
        alumno.setFechaNacimiento("2015-01-01");
        return alumno;
    }
}
