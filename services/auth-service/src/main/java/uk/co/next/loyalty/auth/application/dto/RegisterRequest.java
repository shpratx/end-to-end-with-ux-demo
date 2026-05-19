package uk.co.next.loyalty.auth.application.dto;

import jakarta.validation.constraints.*;

public record RegisterRequest(
        @NotBlank @Size(min = 1, max = 255) String name,
        @NotBlank @Email String email,
        @NotBlank @Pattern(regexp = "^\\+[1-9]\\d{1,14}$") String phone,
        @NotBlank @Size(min = 12) String password,
        @NotBlank String termsAndConditionsVersion
) {}
