package uk.co.next.loyalty.auth.application.command;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.next.loyalty.auth.domain.model.Customer;
import uk.co.next.loyalty.auth.domain.model.OtpCode;
import uk.co.next.loyalty.auth.domain.port.CustomerRepository;
import uk.co.next.loyalty.auth.domain.port.OtpRepository;
import uk.co.next.loyalty.auth.domain.port.TokenRepository;

import java.util.Map;
import java.util.UUID;

@Service
public class ConfirmPasswordResetCommand {

    private final OtpRepository otpRepository;
    private final CustomerRepository customerRepository;
    private final TokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;

    public ConfirmPasswordResetCommand(OtpRepository otpRepository,
                                       CustomerRepository customerRepository,
                                       TokenRepository tokenRepository,
                                       PasswordEncoder passwordEncoder) {
        this.otpRepository = otpRepository;
        this.customerRepository = customerRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public Map<String, Boolean> execute(String token, String newPassword, UUID customerId) {
        OtpCode otp = otpRepository.findLatestUnusedByCustomerId(customerId)
                .orElseThrow(() -> new IllegalArgumentException("TOKEN_INVALID"));

        if (otp.isExpired()) throw new IllegalStateException("TOKEN_EXPIRED");
        if (!passwordEncoder.matches(token, otp.getCodeHash())) {
            throw new IllegalArgumentException("TOKEN_INVALID");
        }

        otp.markUsed();
        otpRepository.save(otp);

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("CUSTOMER_NOT_FOUND"));
        customer.setPasswordHash(passwordEncoder.encode(newPassword));
        customerRepository.save(customer);

        tokenRepository.revokeAllByCustomerId(customerId);

        return Map.of("success", true);
    }
}
