package es.daw.eventhubmvc.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UserProfileUpdateRequest(
        @Size(min = 0,max = 100, message="El tamaño debe estar entre 0 y 100")
        String fullname,
        @NotBlank(message="El email es obligatorio")
        @NotNull(message="El email es obligatorio")
        @Email(message="El email debe tener un formato correcto")
        @Size(min = 0, max=50, message="El tamaño debe estar entre 0 y 50")
        String email,

        String password,
        String confirmPassword
) {}
