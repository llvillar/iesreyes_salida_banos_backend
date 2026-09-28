package es.ies.reyes.gestion.banos.services;

import es.ies.reyes.gestion.banos.dto.PermisoBanoRequest;
import es.ies.reyes.gestion.banos.dto.PermisoBanoResponse;
import es.ies.reyes.gestion.banos.dto.PermisoBanoFiltro;
import es.ies.reyes.gestion.banos.models.Alumno;
import es.ies.reyes.gestion.banos.models.FranjaHoraria;
import es.ies.reyes.gestion.banos.models.PermisoBano;
import es.ies.reyes.gestion.banos.models.Profesor;
import es.ies.reyes.gestion.banos.repositories.AlumnoRepository;
import es.ies.reyes.gestion.banos.repositories.FranjaHorariaRepository;
import es.ies.reyes.gestion.banos.repositories.PermisoBanoRepository;
import es.ies.reyes.gestion.banos.repositories.ProfesorRepository;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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
    public List<PermisoBanoResponse> listar(PermisoBanoFiltro filtro) {
        Specification<PermisoBano> specification = (root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();
            Join<Object, Object> alumno = root.join("alumno");
            Join<Object, Object> profesor = root.join("profesor");
            Join<Object, Object> franja = root.join("franjaHoraria");

            if (filtro.fecha() != null) {
                predicates.add(builder.equal(root.get("fecha"), filtro.fecha()));
            }
            if (filtro.desde() != null) {
                predicates.add(builder.greaterThanOrEqualTo(root.get("fecha"), filtro.desde()));
            }
            if (filtro.hasta() != null) {
                predicates.add(builder.lessThanOrEqualTo(root.get("fecha"), filtro.hasta()));
            }
            if (filtro.grupo() != null && !filtro.grupo().isBlank()) {
                predicates.add(builder.equal(
                        builder.lower(alumno.join("grupo").get("codigo")),
                        filtro.grupo().trim().toLowerCase(Locale.ROOT)
                ));
            }
            if (filtro.grupoId() != null) {
                predicates.add(builder.equal(alumno.join("grupo").get("id"), filtro.grupoId()));
            }
            if (filtro.alumnoId() != null) {
                predicates.add(builder.equal(alumno.get("id"), filtro.alumnoId()));
            }
            if (filtro.alumnoDni() != null && !filtro.alumnoDni().isBlank()) {
                predicates.add(builder.equal(
                        builder.lower(alumno.get("dni")),
                        filtro.alumnoDni().trim().toLowerCase(Locale.ROOT)
                ));
            }
            if (filtro.profesorId() != null) {
                predicates.add(builder.equal(profesor.get("id"), filtro.profesorId()));
            }
            if (filtro.profesorDni() != null && !filtro.profesorDni().isBlank()) {
                predicates.add(builder.equal(
                        builder.lower(profesor.get("dni")),
                        filtro.profesorDni().trim().toLowerCase(Locale.ROOT)
                ));
            }
            if (filtro.franjaHorariaId() != null) {
                predicates.add(builder.equal(franja.get("id"), filtro.franjaHorariaId()));
            }
            if (filtro.numeroFranja() != null) {
                predicates.add(builder.equal(franja.get("numero"), filtro.numeroFranja()));
            }
            if (filtro.horaDesde() != null) {
                predicates.add(builder.greaterThanOrEqualTo(root.get("hora"), filtro.horaDesde()));
            }
            if (filtro.horaHasta() != null) {
                predicates.add(builder.lessThanOrEqualTo(root.get("hora"), filtro.horaHasta()));
            }
            return builder.and(predicates.toArray(Predicate[]::new));
        };

        Sort sort = Sort.by(Sort.Order.desc("fecha"), Sort.Order.desc("hora"), Sort.Order.desc("id"));
        return repository.findAll(specification, sort).stream()
                .map(PermisoBanoResponse::desde)
                .toList();
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
