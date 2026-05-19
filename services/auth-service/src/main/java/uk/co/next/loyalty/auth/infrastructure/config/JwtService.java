package uk.co.Dunelm.loyalty.auth.infrastructure.config;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {

    private final SecretKey key;
    private final int accessTokenExpiry;
    private final int refreshTokenExpiry;

    public JwtService(@Value("${app.jwt.secret}") String secret,
                      @Value("${app.jwt.access-token-expiry}") int accessTokenExpiry,
                      @Value("${app.jwt.refresh-token-expiry-mobile}") int refreshTokenExpiry) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpiry = accessTokenExpiry;
        this.refreshTokenExpiry = refreshTokenExpiry;
    }

    public String generateAccessToken(UUID customerId) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(customerId.toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(accessTokenExpiry)))
                .signWith(key)
                .compact();
    }

    public UUID extractCustomerId(String token) {
        String subject = Jwts.parser().verifyWith(key).build()
                .parseSignedClaims(token).getPayload().getSubject();
        return UUID.fromString(subject);
    }

    public boolean isTokenValid(String token) {
        try {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
            return true;
        } catch (Exception e) { return false; }
    }

    public int getAccessTokenExpiry() { return accessTokenExpiry; }
    public int getRefreshTokenExpiry() { return refreshTokenExpiry; }
}
