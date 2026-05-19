package uk.co.Dunelm.loyalty.auth.application.command;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.Dunelm.loyalty.auth.application.dto.AuthTokenResponse;
import uk.co.Dunelm.loyalty.auth.domain.model.RefreshToken;
import uk.co.Dunelm.loyalty.auth.domain.port.TokenRepository;
import uk.co.Dunelm.loyalty.auth.infrastructure.config.JwtService;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.UUID;

@Service
public class RefreshTokenCommand {

    private final TokenRepository tokenRepository;
    private final JwtService jwtService;

    public RefreshTokenCommand(TokenRepository tokenRepository, JwtService jwtService) {
        this.tokenRepository = tokenRepository;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthTokenResponse execute(String refreshTokenRaw) {
        String hash = sha256(refreshTokenRaw);
        RefreshToken token = tokenRepository.findByTokenHash(hash)
                .orElseThrow(() -> new IllegalArgumentException("TOKEN_NOT_FOUND"));

        if (token.isRevoked()) throw new IllegalStateException("TOKEN_REVOKED");
        if (token.isExpired()) throw new IllegalStateException("TOKEN_EXPIRED");

        // Token rotation
        token.revoke();
        tokenRepository.save(token);

        String newAccessToken = jwtService.generateAccessToken(token.getCustomerId());
        String newRefreshRaw = UUID.randomUUID().toString();
        tokenRepository.save(new RefreshToken(
                token.getCustomerId(), sha256(newRefreshRaw),
                Instant.now().plusSeconds(jwtService.getRefreshTokenExpiry()), null
        ));

        return new AuthTokenResponse(newAccessToken, newRefreshRaw, jwtService.getAccessTokenExpiry());
    }

    private String sha256(String input) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) { throw new RuntimeException(e); }
    }
}
