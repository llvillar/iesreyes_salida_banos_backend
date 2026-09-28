package es.ies.reyes.gestion.banos.repositories;

import es.ies.reyes.gestion.banos.models.Alumno;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlumnoRepository extends JpaRepository<Alumno, Long> {
}
