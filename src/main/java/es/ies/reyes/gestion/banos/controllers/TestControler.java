package es.ies.reyes.gestion.banos.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class TestControler {

    @GetMapping("/hola")
    public String saludar(){
        return "¡Servidor del IES configurado y funcionando correctamente!";
    }
}
