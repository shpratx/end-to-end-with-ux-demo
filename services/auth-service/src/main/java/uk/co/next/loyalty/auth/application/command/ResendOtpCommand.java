package uk.co.next.loyalty.auth.application.command;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.next.loyalty.auth.domain.model.OtpCode;
import uk.co.next.loyalty.auth.domain.port.CustomerRepository;
import uk.co.next.loyalty.auth.domain.port.OtpRepository;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Service
public class ResendOtpCommand {

    private final OtpRepository otpRepository;
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecureRandom secureRandom = new SecureRandom();

    public ResendOtpCommand(OtpRepository otpRepository,
                            CustomerRepository customerRepository,
                            PasswordEncoder passwordEncoder) {
        this.otpRepository = otpRepository;
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public Map<String, Object> execute(UUID customerId) {
        customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("CUSTOMER_NOT_FOUND"));

        long recentCount = otpRepository.countByCustomerIdSince(customerId, Instant.now().minusSeconds(600));
        if (recentCount >= 3) {
            throw new IllegalStateException("RATE_LIMIT_EXCEEDED");
        }

        String otpPlain = String.format("%06d", secureRandom.nextInt(1_000_000));
        otpRepository.save(new OtpCode(
                customerId, passwordEncoder.encode(otpPlain),
                OtpCode.OtpPurpose.REGISTRATION,
                Instant.now().plusSeconds(300)
        ));

        return Map.of(
                "sent", true,
                "nextResendAt", Instant.now().plusSeconds(60).toString()
        );
    }
}
