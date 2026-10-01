package es.ies.reyes.gestion.banos.repositories;

import es.ies.reyes.gestion.banos.models.Profesor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProfesorRepository extends JpaRepository<Profesor, Long> {
    Optional<Profesor> findByDni(String dni);
}
