package es.ies.reyes.gestion.banos.security;

import es.ies.reyes.gestion.banos.repositories.UsuarioAppRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class UsuarioAppDetailsService implements UserDetailsService {

    private final UsuarioAppRepository usuarioRepository;

    public UsuarioAppDetailsService(UsuarioAppRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        String usuarioNormalizado = username.trim().toLowerCase(Locale.ROOT);
        return usuarioRepository.findByProfesorEmailIgnoreCase(usuarioNormalizado)
                .map(usuario -> User.withUsername(usuario.getProfesor().getEmail().trim().toLowerCase(Locale.ROOT))
                        .password(usuario.getContrasenaHash())
                        .roles(usuario.getPerfil().name())
                        .build())
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
    }
}
