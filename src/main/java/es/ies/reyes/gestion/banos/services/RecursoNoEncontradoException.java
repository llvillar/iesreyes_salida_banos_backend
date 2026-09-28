package es.ies.reyes.gestion.banos.services;

public class RecursoNoEncontradoException extends RuntimeException {

    public RecursoNoEncontradoException(String tipo, Long id) {
        super("No existe " + tipo + " con id " + id);
    }
}
