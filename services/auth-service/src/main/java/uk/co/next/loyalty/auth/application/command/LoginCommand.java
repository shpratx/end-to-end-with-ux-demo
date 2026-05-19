package uk.co.Dunelm.loyalty.auth.application.command;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.Dunelm.loyalty.auth.application.dto.AuthTokenResponse;
import uk.co.Dunelm.loyalty.auth.application.dto.LoginRequest;
import uk.co.Dunelm.loyalty.auth.domain.model.Customer;
import uk.co.Dunelm.loyalty.auth.domain.model.RefreshToken;
import uk.co.Dunelm.loyalty.auth.domain.port.CustomerRepository;
import uk.co.Dunelm.loyalty.auth.domain.port.TokenRepository;
import uk.co.Dunelm.loyalty.auth.infrastructure.config.JwtService;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.UUID;

@Service
public class LoginCommand {

    private final CustomerRepository customerRepository;
    private final TokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public LoginCommand(CustomerRepository customerRepository,
                        TokenRepository tokenRepository,
                        PasswordEncoder passwordEncoder,
                        JwtService jwtService) {
        this.customerRepository = customerRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthTokenResponse execute(LoginRequest request) {
        Customer customer = customerRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalArgumentException("INVALID_CREDENTIALS"));

        if (customer.isLocked()) {
            throw new IllegalStateException("ACCOUNT_LOCKED");
        }
        if (customer.getStatus() == Customer.CustomerStatus.PENDING_VERIFICATION) {
            throw new IllegalStateException("ACCOUNT_NOT_VERIFIED");
        }
        if (customer.getStatus() == Customer.CustomerStatus.SUSPENDED) {
            throw new IllegalStateException("ACCOUNT_SUSPENDED");
        }

        if (!passwordEncoder.matches(request.password(), customer.getPasswordHash())) {
            customer.recordFailedLogin();
            customerRepository.save(customer);
            throw new IllegalArgumentException("INVALID_CREDENTIALS");
        }

        customer.resetFailedLogins();
        customerRepository.save(customer);

        String accessToken = jwtService.generateAccessToken(customer.getId());
        String refreshTokenRaw = UUID.randomUUID().toString();
        String refreshTokenHash = sha256(refreshTokenRaw);

        RefreshToken refreshToken = new RefreshToken(
                customer.getId(), refreshTokenHash,
                Instant.now().plusSeconds(jwtService.getRefreshTokenExpiry()), null
        );
        tokenRepository.save(refreshToken);

        return new AuthTokenResponse(accessToken, refreshTokenRaw, jwtService.getAccessTokenExpiry());
    }

    private String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
