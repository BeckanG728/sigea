package com.institucion.sigea.usuario.config;

import com.institucion.sigea.auth.service.TotpService;
import com.institucion.sigea.usuario.entity.Rol;
import com.institucion.sigea.usuario.entity.Usuario;
import com.institucion.sigea.usuario.repository.RolRepository;
import com.institucion.sigea.usuario.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@Profile("!test")
@Order(1)
@RequiredArgsConstructor
public class SuperusuarioSeeder implements CommandLineRunner {

    private static final String NOMBRE_ROL_SUPERUSUARIO = "SUPERUSUARIO";

    private final RolRepository rolRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final TotpService totpService;

    @Value("${superuser.username}")
    private String superuserUsername;

    @Value("${superuser.password}")
    private String superuserPassword;

    @Override
    @Transactional
    public void run(String... args) {
        Rol rol = rolRepository.findByNombre(NOMBRE_ROL_SUPERUSUARIO)
                .orElseGet(() -> {
                    Rol nuevoRol = new Rol(NOMBRE_ROL_SUPERUSUARIO);
                    nuevoRol = rolRepository.save(nuevoRol);
                    log.info("Rol '{}' creado", NOMBRE_ROL_SUPERUSUARIO);
                    return nuevoRol;
                });

        if (superuserUsername.isBlank() || superuserPassword.isBlank()) {
            log.warn("SUPERUSER_USERNAME/SUPERUSER_PASSWORD no configurados — se omite creacion de usuario");
            return;
        }

        if (!usuarioRepository.existsByNombreUsuario(superuserUsername)) {
            Usuario usuario = new Usuario();
            usuario.setNombreUsuario(superuserUsername);
            usuario.setPassword(passwordEncoder.encode(superuserPassword));
            usuario.setRol(rol);
            usuario.setLogin2fa(false);
            usuario.setNombre("Super");
            usuario.setPrimerApellido("Usuario");
            usuario.setNumeroDocumento("00000000");
            usuario.setCodigo("CU-000");
            usuario.setTotpSecret(totpService.generarSecreto(superuserUsername).secretRaw());
            usuario.setTotpVerificado(false);
            usuarioRepository.save(usuario);
            log.info("Superusuario '{}' creado correctamente", superuserUsername);
        } else {
            log.debug("Superusuario '{}' ya existe — se omite creacion", superuserUsername);
        }
    }
}