package uk.co.Dunelm.loyalty.auth.application.command;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.Dunelm.loyalty.auth.domain.model.Customer;
import uk.co.Dunelm.loyalty.auth.domain.model.OtpCode;
import uk.co.Dunelm.loyalty.auth.domain.model.RefreshToken;
import uk.co.Dunelm.loyalty.auth.domain.port.CustomerRepository;
import uk.co.Dunelm.loyalty.auth.domain.port.OtpRepository;
import uk.co.Dunelm.loyalty.auth.domain.port.TokenRepository;
import uk.co.Dunelm.loyalty.auth.infrastructure.config.JwtService;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Map;
import java.util.UUID;

@Service
public class VerifyOtpCommand {

    private final OtpRepository otpRepository;
    private final CustomerRepository customerRepository;
    private final TokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public VerifyOtpCommand(OtpRepository otpRepository, CustomerRepository customerRepository,
                            TokenRepository tokenRepository, PasswordEncoder passwordEncoder,
                            JwtService jwtService) {
        this.otpRepository = otpRepository;
        this.customerRepository = customerRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public Map<String, Object> execute(UUID customerId, String otpCode) {
        OtpCode otp = otpRepository.findLatestUnusedByCustomerId(customerId)
                .orElseThrow(() -> new IllegalArgumentException("OTP_INVALID"));

        if (otp.isExpired()) throw new IllegalStateException("OTP_EXPIRED");
        if (otp.getAttempts() >= 5) throw new IllegalStateException("ACCOUNT_LOCKED");

        otp.incrementAttempts();

        if (!passwordEncoder.matches(otpCode, otp.getCodeHash())) {
            otpRepository.save(otp);
            throw new IllegalArgumentException("OTP_INVALID");
        }

        otp.markUsed();
        otpRepository.save(otp);

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("CUSTOMER_NOT_FOUND"));
        customer.activate();
        customerRepository.save(customer);

        String accessToken = jwtService.generateAccessToken(customerId);
        String refreshTokenRaw = UUID.randomUUID().toString();
        tokenRepository.save(new RefreshToken(
                customerId, sha256(refreshTokenRaw),
                Instant.now().plusSeconds(jwtService.getRefreshTokenExpiry()), null
        ));

        return Map.of(
                "verified", true,
                "accessToken", accessToken,
                "refreshToken", refreshTokenRaw,
                "expiresIn", jwtService.getAccessTokenExpiry()
        );
    }

    private String sha256(String input) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256")
                    .digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) { throw new RuntimeException(e); }
    }
}
