package es.ies.reyes.gestion.banos.controllers;

import es.ies.reyes.gestion.banos.dto.AlumnoRequest;
import es.ies.reyes.gestion.banos.dto.AlumnoResponse;
import es.ies.reyes.gestion.banos.dto.FranjaHorariaRequest;
import es.ies.reyes.gestion.banos.dto.FranjaHorariaResponse;
import es.ies.reyes.gestion.banos.dto.GrupoRequest;
import es.ies.reyes.gestion.banos.dto.GrupoResponse;
import es.ies.reyes.gestion.banos.dto.ProfesorRequest;
import es.ies.reyes.gestion.banos.dto.ProfesorResponse;
import es.ies.reyes.gestion.banos.services.CatalogoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class CatalogoController {

    private final CatalogoService service;

    public CatalogoController(CatalogoService service) {
        this.service = service;
    }

    @GetMapping("/alumnos")
    public List<AlumnoResponse> listarAlumnos() {
        return service.listarAlumnos();
    }

    @GetMapping("/alumnos/{id}")
    public AlumnoResponse buscarAlumno(@PathVariable Long id) {
        return service.buscarAlumno(id);
    }

    @PostMapping("/alumnos")
    @ResponseStatus(HttpStatus.CREATED)
    public AlumnoResponse crearAlumno(@Valid @RequestBody AlumnoRequest request) {
        return service.crearAlumno(request);
    }

    @PutMapping("/alumnos/{id}")
    public AlumnoResponse actualizarAlumno(
            @PathVariable Long id,
            @Valid @RequestBody AlumnoRequest request) {
        return service.actualizarAlumno(id, request);
    }

    @DeleteMapping("/alumnos/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarAlumno(@PathVariable Long id) {
        service.eliminarAlumno(id);
    }

    @GetMapping("/profesores")
    public List<ProfesorResponse> listarProfesores() {
        return service.listarProfesores();
    }

    @GetMapping("/profesores/{id}")
    public ProfesorResponse buscarProfesor(@PathVariable Long id) {
        return service.buscarProfesor(id);
    }

    @PostMapping("/profesores")
    @ResponseStatus(HttpStatus.CREATED)
    public ProfesorResponse crearProfesor(@Valid @RequestBody ProfesorRequest request) {
        return service.crearProfesor(request);
    }

    @PutMapping("/profesores/{id}")
    public ProfesorResponse actualizarProfesor(
            @PathVariable Long id,
            @Valid @RequestBody ProfesorRequest request) {
        return service.actualizarProfesor(id, request);
    }

    @DeleteMapping("/profesores/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarProfesor(@PathVariable Long id) {
        service.eliminarProfesor(id);
    }

    @GetMapping("/grupos")
    public List<GrupoResponse> listarGrupos() {
        return service.listarGrupos();
    }

    @GetMapping("/grupos/{id}")
    public GrupoResponse buscarGrupo(@PathVariable Long id) {
        return service.buscarGrupo(id);
    }

    @PostMapping("/grupos")
    @ResponseStatus(HttpStatus.CREATED)
    public GrupoResponse crearGrupo(@Valid @RequestBody GrupoRequest request) {
        return service.crearGrupo(request);
    }

    @PutMapping("/grupos/{id}")
    public GrupoResponse actualizarGrupo(
            @PathVariable Long id,
            @Valid @RequestBody GrupoRequest request) {
        return service.actualizarGrupo(id, request);
    }

    @DeleteMapping("/grupos/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarGrupo(@PathVariable Long id) {
        service.eliminarGrupo(id);
    }

    @GetMapping("/franjas-horarias")
    public List<FranjaHorariaResponse> listarFranjas() {
        return service.listarFranjas();
    }

    @GetMapping("/franjas-horarias/{id}")
    public FranjaHorariaResponse buscarFranja(@PathVariable Long id) {
        return service.buscarFranja(id);
    }

    @PostMapping("/franjas-horarias")
    @ResponseStatus(HttpStatus.CREATED)
    public FranjaHorariaResponse crearFranja(@Valid @RequestBody FranjaHorariaRequest request) {
        return service.crearFranja(request);
    }

    @PutMapping("/franjas-horarias/{id}")
    public FranjaHorariaResponse actualizarFranja(
            @PathVariable Long id,
            @Valid @RequestBody FranjaHorariaRequest request) {
        return service.actualizarFranja(id, request);
    }

    @DeleteMapping("/franjas-horarias/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarFranja(@PathVariable Long id) {
        service.eliminarFranja(id);
    }
}
