package es.ies.reyes.gestion.banos.repositories;

import es.ies.reyes.gestion.banos.models.PerfilUsuario;
import es.ies.reyes.gestion.banos.models.UsuarioApp;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsuarioAppRepository extends JpaRepository<UsuarioApp, Long> {
    @EntityGraph(attributePaths = "profesor")
    Optional<UsuarioApp> findByProfesorEmailIgnoreCase(String email);

    Optional<UsuarioApp> findByProfesorId(Long profesorId);

    boolean existsByProfesorEmailIgnoreCase(String email);

    boolean existsByProfesorId(Long profesorId);

    long countByPerfil(PerfilUsuario perfil);

    List<UsuarioApp> findAllByOrderByEmailAsc();
}
