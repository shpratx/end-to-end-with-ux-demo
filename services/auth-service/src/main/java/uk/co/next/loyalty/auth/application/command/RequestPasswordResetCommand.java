package uk.co.Dunelm.loyalty.auth.application.command;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.Dunelm.loyalty.auth.domain.model.OtpCode;
import uk.co.Dunelm.loyalty.auth.domain.port.CustomerRepository;
import uk.co.Dunelm.loyalty.auth.domain.port.OtpRepository;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Service
public class RequestPasswordResetCommand {

    private final CustomerRepository customerRepository;
    private final OtpRepository otpRepository;
    private final PasswordEncoder passwordEncoder;

    public RequestPasswordResetCommand(CustomerRepository customerRepository,
                                       OtpRepository otpRepository,
                                       PasswordEncoder passwordEncoder) {
        this.customerRepository = customerRepository;
        this.otpRepository = otpRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public Map<String, String> execute(String email) {
        // Always return same response to prevent email enumeration
        customerRepository.findByEmail(email).ifPresent(customer -> {
            String token = UUID.randomUUID().toString();
            String tokenHash = passwordEncoder.encode(token);
            otpRepository.save(new OtpCode(
                    customer.getId(), tokenHash,
                    OtpCode.OtpPurpose.PASSWORD_RESET,
                    Instant.now().plusSeconds(3600)
            ));
            // In production: emit notification event with reset link
        });
        return Map.of("message", "If an account exists with this email, a reset link has been sent");
    }
}
