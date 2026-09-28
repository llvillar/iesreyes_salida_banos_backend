package es.ies.reyes.gestion.banos.repositories;

import es.ies.reyes.gestion.banos.models.Profesor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfesorRepository extends JpaRepository<Profesor, Long> {
}
