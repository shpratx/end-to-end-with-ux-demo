package uk.co.next.loyalty.auth.application.dto;

public record AuthTokenResponse(
        String accessToken,
        String refreshToken,
        int expiresIn,
        String tokenType
) {
    public AuthTokenResponse(String accessToken, String refreshToken, int expiresIn) {
        this(accessToken, refreshToken, expiresIn, "Bearer");
    }
}
