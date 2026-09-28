package es.ies.reyes.gestion.banos.services;

import es.ies.reyes.gestion.banos.dto.PermisoBanoRequest;
import es.ies.reyes.gestion.banos.dto.PermisoBanoResponse;
import es.ies.reyes.gestion.banos.models.Alumno;
import es.ies.reyes.gestion.banos.models.FranjaHoraria;
import es.ies.reyes.gestion.banos.models.PermisoBano;
import es.ies.reyes.gestion.banos.models.Profesor;
import es.ies.reyes.gestion.banos.repositories.AlumnoRepository;
import es.ies.reyes.gestion.banos.repositories.FranjaHorariaRepository;
import es.ies.reyes.gestion.banos.repositories.PermisoBanoRepository;
import es.ies.reyes.gestion.banos.repositories.ProfesorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class PermisoBanoService {

    private final PermisoBanoRepository repository;
    private final AlumnoRepository alumnoRepository;
    private final ProfesorRepository profesorRepository;
    private final FranjaHorariaRepository franjaRepository;

    public PermisoBanoService(PermisoBanoRepository repository,
                              AlumnoRepository alumnoRepository,
                              ProfesorRepository profesorRepository,
                              FranjaHorariaRepository franjaRepository) {
        this.repository = repository;
        this.alumnoRepository = alumnoRepository;
        this.profesorRepository = profesorRepository;
        this.franjaRepository = franjaRepository;
    }

    @Transactional(readOnly = true)
    public List<PermisoBanoResponse> listar(LocalDate fecha, String grupo) {
        List<PermisoBano> permisos;
        if (fecha != null && grupo != null) {
            permisos = repository.findByFechaAndAlumno_Grupo_CodigoIgnoreCase(fecha, grupo);
        } else if (fecha != null) {
            permisos = repository.findByFecha(fecha);
        } else if (grupo != null) {
            permisos = repository.findByAlumno_Grupo_CodigoIgnoreCase(grupo);
        } else {
            permisos = repository.findAll();
        }
        return permisos.stream().map(PermisoBanoResponse::desde).toList();
    }

    @Transactional(readOnly = true)
    public PermisoBanoResponse buscar(Long id) {
        return PermisoBanoResponse.desde(buscarEntidad(id));
    }

    public PermisoBanoResponse crear(PermisoBanoRequest request) {
        return PermisoBanoResponse.desde(repository.save(convertir(request)));
    }

    public PermisoBanoResponse actualizar(Long id, PermisoBanoRequest request) {
        PermisoBano permiso = buscarEntidad(id);
        permiso.actualizar(
                buscarAlumno(request.alumnoId()),
                buscarProfesor(request.profesorId()),
                buscarFranja(request.franjaHorariaId()),
                request.fecha(),
                request.hora()
        );
        return PermisoBanoResponse.desde(repository.save(permiso));
    }

    public void eliminar(Long id) {
        repository.delete(buscarEntidad(id));
    }

    private PermisoBano buscarEntidad(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("permiso de baño", id));
    }

    private PermisoBano convertir(PermisoBanoRequest request) {
        return new PermisoBano(
                buscarAlumno(request.alumnoId()),
                buscarProfesor(request.profesorId()),
                buscarFranja(request.franjaHorariaId()),
                request.fecha(),
                request.hora()
        );
    }

    private Alumno buscarAlumno(Long id) {
        return alumnoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("alumno", id));
    }

    private Profesor buscarProfesor(Long id) {
        return profesorRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("profesor", id));
    }

    private FranjaHoraria buscarFranja(Long id) {
        return franjaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("franja horaria", id));
    }
}
