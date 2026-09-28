package es.ies.reyes.gestion.banos.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record GrupoRequest(
        @NotBlank
        @Pattern(
                regexp = "1º ESO|2º ESO|3º ESO|4º ESO|1º Bachillerato|2º Bachillerato",
                message = "debe ser un curso de ESO o Bachillerato entre 1º y 4º ESO, o 1º y 2º Bachillerato"
        )
        String curso,
        @NotBlank @Pattern(regexp = "A|B", message = "debe ser A o B") String seccion
) {
    public String codigo() {
        return curso.substring(0, 2) + seccion + " " + curso.substring(3);
    }
}
