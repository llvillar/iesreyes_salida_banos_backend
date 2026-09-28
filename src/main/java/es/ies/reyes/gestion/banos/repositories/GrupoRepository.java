package es.ies.reyes.gestion.banos.repositories;

import es.ies.reyes.gestion.banos.models.Grupo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GrupoRepository extends JpaRepository<Grupo, Long> {
}
