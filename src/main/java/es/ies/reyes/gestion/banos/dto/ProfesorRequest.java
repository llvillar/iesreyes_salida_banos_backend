package es.ies.reyes.gestion.banos.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ProfesorRequest(
        @NotBlank @Pattern(regexp = "[0-9]{8}[A-Za-z]", message = "debe tener 8 números y una letra")
        String dni,
        @NotBlank @Size(max = 80) String nombre,
        @NotBlank @Size(max = 120) String apellidos,
        @Email @Size(max = 150) String email
) {
}
