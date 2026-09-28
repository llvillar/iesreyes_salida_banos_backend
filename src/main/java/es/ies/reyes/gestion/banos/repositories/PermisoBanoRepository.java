package es.ies.reyes.gestion.banos.repositories;

import es.ies.reyes.gestion.banos.models.PermisoBano;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface PermisoBanoRepository extends JpaRepository<PermisoBano, Long> {

    List<PermisoBano> findByFecha(LocalDate fecha);

    List<PermisoBano> findByAlumno_Grupo_CodigoIgnoreCase(String codigoGrupo);

    List<PermisoBano> findByFechaAndAlumno_Grupo_CodigoIgnoreCase(LocalDate fecha, String codigoGrupo);
}
