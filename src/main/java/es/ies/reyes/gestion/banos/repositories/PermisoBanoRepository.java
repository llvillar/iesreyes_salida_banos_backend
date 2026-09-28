package es.ies.reyes.gestion.banos.repositories;

import es.ies.reyes.gestion.banos.models.PermisoBano;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface PermisoBanoRepository extends JpaRepository<PermisoBano, Long>,
        JpaSpecificationExecutor<PermisoBano> {
}
