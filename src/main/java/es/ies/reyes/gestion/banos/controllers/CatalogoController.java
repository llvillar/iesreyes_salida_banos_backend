package es.ies.reyes.gestion.banos.controllers;

import es.ies.reyes.gestion.banos.dto.AlumnoResponse;
import es.ies.reyes.gestion.banos.dto.FranjaHorariaResponse;
import es.ies.reyes.gestion.banos.dto.GrupoResponse;
import es.ies.reyes.gestion.banos.dto.ProfesorResponse;
import es.ies.reyes.gestion.banos.services.CatalogoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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

    @GetMapping("/profesores")
    public List<ProfesorResponse> listarProfesores() {
        return service.listarProfesores();
    }

    @GetMapping("/grupos")
    public List<GrupoResponse> listarGrupos() {
        return service.listarGrupos();
    }

    @GetMapping("/franjas-horarias")
    public List<FranjaHorariaResponse> listarFranjas() {
        return service.listarFranjas();
    }
}
