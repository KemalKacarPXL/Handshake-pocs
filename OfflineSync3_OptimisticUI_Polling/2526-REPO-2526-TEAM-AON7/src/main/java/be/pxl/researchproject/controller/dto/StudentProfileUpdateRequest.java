package be.pxl.researchproject.controller.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record StudentProfileUpdateRequest(
        @NotBlank(message = "Voornaam mag niet leeg zijn")
        String firstname,
        @NotBlank(message = "Achternaam mag niet leeg zijn")
        String lastname,
        @NotBlank(message = "Email mag niet leeg zijn")
        @Email(message = "Ongeldig emailadres")
        String email,
        @NotBlank(message = "Opleiding mag niet leeg zijn")
        String education
) {
}
