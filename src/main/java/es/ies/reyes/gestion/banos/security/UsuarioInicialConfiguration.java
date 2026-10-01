package es.ies.reyes.gestion.banos.security;

import es.ies.reyes.gestion.banos.models.PerfilUsuario;
import es.ies.reyes.gestion.banos.models.UsuarioApp;
import es.ies.reyes.gestion.banos.repositories.ProfesorRepository;
import es.ies.reyes.gestion.banos.repositories.UsuarioAppRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

@Configuration
public class UsuarioInicialConfiguration {

    @Bean
    ApplicationRunner crearUsuarioInicial(
            UsuarioAppRepository usuarioRepository,
            ProfesorRepository profesorRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.auth.initial-password}") String password,
            @Value("${app.auth.initial-profesor-dni}") String profesorDni) {
        return arguments -> {
            if (usuarioRepository.count() > 0) {
                return;
            }
            if (password.length() < 12
                    || password.getBytes(StandardCharsets.UTF_8).length > 72 || profesorDni.isBlank()) {
                throw new IllegalArgumentException(
                        "Configura una contraseña de 12 a 72 bytes y el DNI del profesor inicial");
            }
            var profesor = profesorRepository.findByDni(profesorDni.trim().toUpperCase(Locale.ROOT))
                    .orElseThrow(() -> new IllegalStateException(
                            "No existe el profesor configurado para el usuario inicial: " + profesorDni));
            if (profesor.getEmail() == null || profesor.getEmail().isBlank()) {
                throw new IllegalStateException("El profesor inicial debe tener un correo electrónico");
            }
            usuarioRepository.save(new UsuarioApp(
                    profesor.getEmail().trim().toLowerCase(Locale.ROOT),
                    passwordEncoder.encode(password),
                    PerfilUsuario.GESTION_CATALOGOS,
                    profesor
            ));
        };
    }
}
