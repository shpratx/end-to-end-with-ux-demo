package uk.co.next.loyalty.auth.application.dto;

import jakarta.validation.constraints.*;
import java.util.UUID;

public record OtpVerifyRequest(
        @NotNull UUID customerId,
        @NotBlank @Pattern(regexp = "^\\d{6}$") String otpCode
) {}
