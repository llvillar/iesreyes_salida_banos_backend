package es.ies.reyes.gestion.banos.services;

import es.ies.reyes.gestion.banos.dto.AlumnoRequest;
import es.ies.reyes.gestion.banos.dto.AlumnoResponse;
import es.ies.reyes.gestion.banos.dto.FranjaHorariaRequest;
import es.ies.reyes.gestion.banos.dto.FranjaHorariaResponse;
import es.ies.reyes.gestion.banos.dto.GrupoRequest;
import es.ies.reyes.gestion.banos.dto.GrupoResponse;
import es.ies.reyes.gestion.banos.dto.ProfesorRequest;
import es.ies.reyes.gestion.banos.dto.ProfesorResponse;
import es.ies.reyes.gestion.banos.models.Alumno;
import es.ies.reyes.gestion.banos.models.FranjaHoraria;
import es.ies.reyes.gestion.banos.models.Grupo;
import es.ies.reyes.gestion.banos.models.Profesor;
import es.ies.reyes.gestion.banos.repositories.AlumnoRepository;
import es.ies.reyes.gestion.banos.repositories.FranjaHorariaRepository;
import es.ies.reyes.gestion.banos.repositories.GrupoRepository;
import es.ies.reyes.gestion.banos.repositories.ProfesorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CatalogoService {

    private final AlumnoRepository alumnoRepository;
    private final ProfesorRepository profesorRepository;
    private final GrupoRepository grupoRepository;
    private final FranjaHorariaRepository franjaRepository;
    private final UsuarioAppService usuarioAppService;

    public CatalogoService(AlumnoRepository alumnoRepository,
                           ProfesorRepository profesorRepository,
                           GrupoRepository grupoRepository,
                           FranjaHorariaRepository franjaRepository,
                           UsuarioAppService usuarioAppService) {
        this.alumnoRepository = alumnoRepository;
        this.profesorRepository = profesorRepository;
        this.grupoRepository = grupoRepository;
        this.franjaRepository = franjaRepository;
        this.usuarioAppService = usuarioAppService;
    }

    @Transactional(readOnly = true)
    public List<AlumnoResponse> listarAlumnos() {
        return alumnoRepository.findAll().stream().map(AlumnoResponse::desde).toList();
    }

    @Transactional(readOnly = true)
    public AlumnoResponse buscarAlumno(Long id) {
        return AlumnoResponse.desde(obtenerAlumno(id));
    }

    public AlumnoResponse crearAlumno(AlumnoRequest request) {
        Alumno alumno = new Alumno(
                request.dni().trim().toUpperCase(),
                request.nombre().trim(),
                request.apellidos().trim(),
                normalizarEmail(request.email()),
                obtenerGrupo(request.grupoId())
        );
        return AlumnoResponse.desde(alumnoRepository.save(alumno));
    }

    public AlumnoResponse actualizarAlumno(Long id, AlumnoRequest request) {
        Alumno alumno = obtenerAlumno(id);
        alumno.actualizar(
                request.dni().trim().toUpperCase(),
                request.nombre().trim(),
                request.apellidos().trim(),
                normalizarEmail(request.email()),
                obtenerGrupo(request.grupoId())
        );
        return AlumnoResponse.desde(alumnoRepository.save(alumno));
    }

    public void eliminarAlumno(Long id) {
        alumnoRepository.delete(obtenerAlumno(id));
    }

    @Transactional(readOnly = true)
    public List<ProfesorResponse> listarProfesores() {
        return profesorRepository.findAll().stream().map(ProfesorResponse::desde).toList();
    }

    @Transactional(readOnly = true)
    public ProfesorResponse buscarProfesor(Long id) {
        return ProfesorResponse.desde(obtenerProfesor(id));
    }

    public ProfesorResponse crearProfesor(ProfesorRequest request) {
        Profesor profesor = new Profesor(
                request.dni().trim().toUpperCase(),
                request.nombre().trim(),
                request.apellidos().trim(),
                normalizarEmail(request.email())
        );
        Profesor guardado = profesorRepository.save(profesor);
        usuarioAppService.sincronizarEmailProfesor(guardado);
        return ProfesorResponse.desde(guardado);
    }

    public ProfesorResponse actualizarProfesor(Long id, ProfesorRequest request) {
        Profesor profesor = obtenerProfesor(id);
        profesor.actualizar(
                request.dni().trim().toUpperCase(),
                request.nombre().trim(),
                request.apellidos().trim(),
                normalizarEmail(request.email())
        );
        Profesor guardado = profesorRepository.save(profesor);
        usuarioAppService.sincronizarEmailProfesor(guardado);
        return ProfesorResponse.desde(guardado);
    }

    public void eliminarProfesor(Long id) {
        profesorRepository.delete(obtenerProfesor(id));
    }

    @Transactional(readOnly = true)
    public List<GrupoResponse> listarGrupos() {
        return grupoRepository.findAll().stream().map(GrupoResponse::desde).toList();
    }

    @Transactional(readOnly = true)
    public GrupoResponse buscarGrupo(Long id) {
        return GrupoResponse.desde(obtenerGrupo(id));
    }

    public GrupoResponse crearGrupo(GrupoRequest request) {
        return GrupoResponse.desde(grupoRepository.save(
                new Grupo(request.curso(), request.seccion(), request.codigo())
        ));
    }

    public GrupoResponse actualizarGrupo(Long id, GrupoRequest request) {
        Grupo grupo = obtenerGrupo(id);
        grupo.actualizar(request.curso(), request.seccion(), request.codigo());
        return GrupoResponse.desde(grupoRepository.save(grupo));
    }

    public void eliminarGrupo(Long id) {
        grupoRepository.delete(obtenerGrupo(id));
    }

    @Transactional(readOnly = true)
    public List<FranjaHorariaResponse> listarFranjas() {
        return franjaRepository.findAll().stream().map(FranjaHorariaResponse::desde).toList();
    }

    @Transactional(readOnly = true)
    public FranjaHorariaResponse buscarFranja(Long id) {
        return FranjaHorariaResponse.desde(obtenerFranja(id));
    }

    public FranjaHorariaResponse crearFranja(FranjaHorariaRequest request) {
        return FranjaHorariaResponse.desde(franjaRepository.save(
                new FranjaHoraria(request.numero(), request.nombre())
        ));
    }

    public FranjaHorariaResponse actualizarFranja(Long id, FranjaHorariaRequest request) {
        FranjaHoraria franja = obtenerFranja(id);
        franja.actualizar(request.numero(), request.nombre());
        return FranjaHorariaResponse.desde(franjaRepository.save(franja));
    }

    public void eliminarFranja(Long id) {
        franjaRepository.delete(obtenerFranja(id));
    }

    private Alumno obtenerAlumno(Long id) {
        return alumnoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("alumno", id));
    }

    private Profesor obtenerProfesor(Long id) {
        return profesorRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("profesor", id));
    }

    private Grupo obtenerGrupo(Long id) {
        return grupoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("grupo", id));
    }

    private FranjaHoraria obtenerFranja(Long id) {
        return franjaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("franja horaria", id));
    }

    private String normalizarEmail(String email) {
        return email == null || email.isBlank() ? null : email.trim().toLowerCase(java.util.Locale.ROOT);
    }
}
