package es.ies.reyes.gestion.banos.services;

import es.ies.reyes.gestion.banos.dto.CambioContrasenaRequest;
import es.ies.reyes.gestion.banos.dto.PerfilRequest;
import es.ies.reyes.gestion.banos.dto.UsuarioAppRequest;
import es.ies.reyes.gestion.banos.dto.UsuarioAppResponse;
import es.ies.reyes.gestion.banos.models.PerfilUsuario;
import es.ies.reyes.gestion.banos.models.Profesor;
import es.ies.reyes.gestion.banos.models.UsuarioApp;
import es.ies.reyes.gestion.banos.repositories.ProfesorRepository;
import es.ies.reyes.gestion.banos.repositories.UsuarioAppRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
@Transactional
public class UsuarioAppService {

    private final UsuarioAppRepository usuarioRepository;
    private final ProfesorRepository profesorRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioAppService(
            UsuarioAppRepository usuarioRepository,
            ProfesorRepository profesorRepository,
            PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.profesorRepository = profesorRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<UsuarioAppResponse> listarUsuarios() {
        return usuarioRepository.findAllByOrderByEmailAsc().stream()
                .map(this::respuesta)
                .toList();
    }

    public UsuarioAppResponse crearUsuario(UsuarioAppRequest request) {
        validarLongitudContrasena(request.password());
        Profesor profesor = profesorRepository.findById(request.profesorId())
                .orElseThrow(() -> new RecursoNoEncontradoException("profesor", request.profesorId()));
        if (profesor.getEmail() == null || profesor.getEmail().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El profesor debe tener un correo electrónico antes de crear su cuenta"
            );
        }
        if (usuarioRepository.existsByProfesorId(profesor.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El profesor ya tiene una cuenta");
        }
        String email = profesor.getEmail().trim().toLowerCase(java.util.Locale.ROOT);
        if (usuarioRepository.existsByProfesorEmailIgnoreCase(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El correo ya está asociado a otra cuenta");
        }
        UsuarioApp usuario = new UsuarioApp(
                email,
                passwordEncoder.encode(request.password()),
                request.perfil(),
                profesor
        );
        return respuesta(usuarioRepository.save(usuario));
    }

    public void actualizarPerfil(Long id, PerfilRequest request) {
        UsuarioApp usuario = obtenerUsuario(id);
        if (usuario.getPerfil() == PerfilUsuario.GESTION_CATALOGOS
                && request.perfil() != PerfilUsuario.GESTION_CATALOGOS
                && usuarioRepository.countByPerfil(PerfilUsuario.GESTION_CATALOGOS) <= 1) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "No se puede retirar el permiso al último usuario de gestión de catálogos"
            );
        }
        usuario.cambiarPerfil(request.perfil());
    }

    public void eliminarUsuario(Long id, String usuarioActual) {
        UsuarioApp usuario = obtenerUsuario(id);
        if (usuario.getProfesor().getEmail().equalsIgnoreCase(usuarioActual)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "No puedes eliminar tu propia cuenta");
        }
        if (usuario.getPerfil() == PerfilUsuario.GESTION_CATALOGOS
                && usuarioRepository.countByPerfil(PerfilUsuario.GESTION_CATALOGOS) <= 1) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "No se puede eliminar el último usuario de gestión de catálogos"
            );
        }
        usuarioRepository.delete(usuario);
    }

    public void cambiarContrasena(String email, CambioContrasenaRequest request) {
        validarLongitudContrasena(request.contrasenaNueva());
        UsuarioApp usuario = usuarioRepository.findByProfesorEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "La cuenta ya no existe"));
        if (!passwordEncoder.matches(request.contrasenaActual(), usuario.getContrasenaHash())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La contraseña actual no es correcta");
        }
        usuario.cambiarContrasena(passwordEncoder.encode(request.contrasenaNueva()));
    }

    public void sincronizarEmailProfesor(Profesor profesor) {
        usuarioRepository.findByProfesorId(profesor.getId()).ifPresent(usuario -> {
            String email = profesor.getEmail();
            if (email == null || email.isBlank()) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "No se puede quitar el correo a un profesor que tiene una cuenta"
                );
            }
            email = email.trim().toLowerCase(java.util.Locale.ROOT);
            if (usuarioRepository.findByProfesorEmailIgnoreCase(email)
                    .filter(otraCuenta -> !usuario.getId().equals(otraCuenta.getId()))
                    .isPresent()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "El correo ya está asociado a otra cuenta");
            }
            usuario.cambiarEmail(email);
        });
    }

    private void validarLongitudContrasena(String contrasena) {
        if (contrasena.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La contraseña no puede superar los 72 bytes en UTF-8"
            );
        }
    }

    private UsuarioApp obtenerUsuario(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("usuario", id));
    }

    private UsuarioAppResponse respuesta(UsuarioApp usuario) {
        Profesor profesor = usuario.getProfesor();
        return new UsuarioAppResponse(
                usuario.getId(),
                usuario.getProfesor().getEmail(),
                usuario.getPerfil(),
                profesor.getId(),
                profesor.getDni(),
                profesor.getNombre(),
                profesor.getApellidos()
        );
    }
}
