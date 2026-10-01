package es.ies.reyes.gestion.banos.controllers;

import es.ies.reyes.gestion.banos.dto.PerfilRequest;
import es.ies.reyes.gestion.banos.dto.UsuarioAppRequest;
import es.ies.reyes.gestion.banos.dto.UsuarioAppResponse;
import es.ies.reyes.gestion.banos.services.UsuarioAppService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
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
@RequestMapping("/api/usuarios")
public class UsuariosController {

    private final UsuarioAppService service;

    public UsuariosController(UsuarioAppService service) {
        this.service = service;
    }

    @GetMapping
    public List<UsuarioAppResponse> listar() {
        return service.listarUsuarios();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioAppResponse crear(@Valid @RequestBody UsuarioAppRequest request) {
        return service.crearUsuario(request);
    }

    @PutMapping("/{id}/perfil")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void actualizarPerfil(@PathVariable Long id, @Valid @RequestBody PerfilRequest request) {
        service.actualizarPerfil(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id, Authentication authentication) {
        service.eliminarUsuario(id, authentication.getName());
    }
}
