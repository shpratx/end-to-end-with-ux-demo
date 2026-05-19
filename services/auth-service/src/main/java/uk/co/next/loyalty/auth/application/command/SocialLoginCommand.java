package uk.co.next.loyalty.auth.application.command;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.next.loyalty.auth.application.dto.AuthTokenResponse;
import uk.co.next.loyalty.auth.domain.model.Customer;
import uk.co.next.loyalty.auth.domain.model.RefreshToken;
import uk.co.next.loyalty.auth.domain.model.SocialAccount;
import uk.co.next.loyalty.auth.domain.port.CustomerRepository;
import uk.co.next.loyalty.auth.domain.port.TokenRepository;
import uk.co.next.loyalty.auth.infrastructure.config.JwtService;
import uk.co.next.loyalty.auth.infrastructure.persistence.JpaSocialAccountRepository;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Map;
import java.util.UUID;

@Service
public class SocialLoginCommand {

    private final JpaSocialAccountRepository socialAccountRepository;
    private final CustomerRepository customerRepository;
    private final TokenRepository tokenRepository;
    private final JwtService jwtService;
    private final SecureRandom secureRandom = new SecureRandom();

    public SocialLoginCommand(JpaSocialAccountRepository socialAccountRepository,
                              CustomerRepository customerRepository,
                              TokenRepository tokenRepository,
                              JwtService jwtService) {
        this.socialAccountRepository = socialAccountRepository;
        this.customerRepository = customerRepository;
        this.tokenRepository = tokenRepository;
        this.jwtService = jwtService;
    }

    @Transactional
    public Map<String, Object> execute(String provider, String idToken) {
        // In production: verify idToken with provider's OIDC endpoint
        // Stub: extract email and provider user ID from token claims
        String providerUserId = extractProviderUserId(idToken);
        String email = extractEmail(idToken);

        var existing = socialAccountRepository.findByProviderAndProviderUserId(provider, providerUserId);
        boolean isNew = existing.isEmpty();
        UUID customerId;

        if (existing.isPresent()) {
            customerId = existing.get().getCustomerId();
        } else {
            // Create new customer
            String loyaltyId = String.format("%010d", secureRandom.nextLong(1_000_000_000L, 9_999_999_999L));
            Customer customer = new Customer(email, null, null, "SOCIAL_LOGIN_NO_PASSWORD", loyaltyId);
            customer.activate();
            customer = customerRepository.save(customer);
            customerId = customer.getId();
            socialAccountRepository.save(new SocialAccount(customerId, provider, providerUserId, email));
        }

        String accessToken = jwtService.generateAccessToken(customerId);
        String refreshTokenRaw = UUID.randomUUID().toString();
        tokenRepository.save(new RefreshToken(customerId, sha256(refreshTokenRaw),
                Instant.now().plusSeconds(jwtService.getRefreshTokenExpiry()), null));

        return Map.of(
                "accessToken", accessToken,
                "refreshToken", refreshTokenRaw,
                "expiresIn", jwtService.getAccessTokenExpiry(),
                "tokenType", "Bearer",
                "isNewAccount", isNew
        );
    }

    private String extractProviderUserId(String idToken) { return UUID.nameUUIDFromBytes(idToken.getBytes()).toString(); }
    private String extractEmail(String idToken) { return "social@example.com"; } // Stub

    private String sha256(String input) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) { throw new RuntimeException(e); }
    }
}
