package es.ies.reyes.gestion.banos.services;

import es.ies.reyes.gestion.banos.dto.AlumnoResponse;
import es.ies.reyes.gestion.banos.dto.FranjaHorariaResponse;
import es.ies.reyes.gestion.banos.dto.GrupoResponse;
import es.ies.reyes.gestion.banos.dto.ProfesorResponse;
import es.ies.reyes.gestion.banos.repositories.AlumnoRepository;
import es.ies.reyes.gestion.banos.repositories.FranjaHorariaRepository;
import es.ies.reyes.gestion.banos.repositories.GrupoRepository;
import es.ies.reyes.gestion.banos.repositories.ProfesorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class CatalogoService {

    private final AlumnoRepository alumnoRepository;
    private final ProfesorRepository profesorRepository;
    private final GrupoRepository grupoRepository;
    private final FranjaHorariaRepository franjaRepository;

    public CatalogoService(AlumnoRepository alumnoRepository,
                           ProfesorRepository profesorRepository,
                           GrupoRepository grupoRepository,
                           FranjaHorariaRepository franjaRepository) {
        this.alumnoRepository = alumnoRepository;
        this.profesorRepository = profesorRepository;
        this.grupoRepository = grupoRepository;
        this.franjaRepository = franjaRepository;
    }

    public List<AlumnoResponse> listarAlumnos() {
        return alumnoRepository.findAll().stream().map(AlumnoResponse::desde).toList();
    }

    public List<ProfesorResponse> listarProfesores() {
        return profesorRepository.findAll().stream().map(ProfesorResponse::desde).toList();
    }

    public List<GrupoResponse> listarGrupos() {
        return grupoRepository.findAll().stream().map(GrupoResponse::desde).toList();
    }

    public List<FranjaHorariaResponse> listarFranjas() {
        return franjaRepository.findAll().stream().map(FranjaHorariaResponse::desde).toList();
    }
}
