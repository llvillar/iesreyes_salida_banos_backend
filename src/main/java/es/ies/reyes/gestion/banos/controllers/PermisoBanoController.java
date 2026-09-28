package es.ies.reyes.gestion.banos.controllers;

import es.ies.reyes.gestion.banos.dto.PermisoBanoRequest;
import es.ies.reyes.gestion.banos.dto.PermisoBanoResponse;
import es.ies.reyes.gestion.banos.dto.PermisoBanoFiltro;
import es.ies.reyes.gestion.banos.services.PermisoBanoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/permisos")
public class PermisoBanoController {

    private final PermisoBanoService service;

    public PermisoBanoController(PermisoBanoService service) {
        this.service = service;
    }

    @GetMapping
    public List<PermisoBanoResponse> listar(
            @RequestParam(required = false) LocalDate fecha,
            @RequestParam(required = false) LocalDate desde,
            @RequestParam(required = false) LocalDate hasta,
            @RequestParam(required = false) String grupo,
            @RequestParam(required = false) Long grupoId,
            @RequestParam(required = false) Long alumnoId,
            @RequestParam(required = false) String alumnoDni,
            @RequestParam(required = false) Long profesorId,
            @RequestParam(required = false) String profesorDni,
            @RequestParam(required = false) Long franjaHorariaId,
            @RequestParam(required = false) Integer numeroFranja,
            @RequestParam(required = false) LocalTime horaDesde,
            @RequestParam(required = false) LocalTime horaHasta) {
        if (desde != null && hasta != null && desde.isAfter(hasta)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "'desde' no puede ser posterior a 'hasta'");
        }
        if (horaDesde != null && horaHasta != null && horaDesde.isAfter(horaHasta)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "'horaDesde' no puede ser posterior a 'horaHasta'");
        }
        return service.listar(new PermisoBanoFiltro(
                fecha, desde, hasta, grupo, grupoId, alumnoId, alumnoDni,
                profesorId, profesorDni, franjaHorariaId, numeroFranja, horaDesde, horaHasta
        ));
    }

    @GetMapping("/{id}")
    public PermisoBanoResponse buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PermisoBanoResponse crear(@Valid @RequestBody PermisoBanoRequest request) {
        return service.crear(request);
    }

    @PutMapping("/{id}")
    public PermisoBanoResponse actualizar(
            @PathVariable Long id,
            @Valid @RequestBody PermisoBanoRequest request) {
        return service.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        service.eliminar(id);
    }
}
