package uk.co.next.loyalty.auth.application.command;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.next.loyalty.auth.application.dto.RegisterRequest;
import uk.co.next.loyalty.auth.domain.model.Consent;
import uk.co.next.loyalty.auth.domain.model.Customer;
import uk.co.next.loyalty.auth.domain.model.OtpCode;
import uk.co.next.loyalty.auth.domain.port.CustomerRepository;
import uk.co.next.loyalty.auth.domain.port.OtpRepository;
import uk.co.next.loyalty.auth.infrastructure.persistence.JpaConsentRepository;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Service
public class RegisterCustomerCommand {

    private final CustomerRepository customerRepository;
    private final OtpRepository otpRepository;
    private final JpaConsentRepository consentRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecureRandom secureRandom = new SecureRandom();

    public RegisterCustomerCommand(CustomerRepository customerRepository,
                                   OtpRepository otpRepository,
                                   JpaConsentRepository consentRepository,
                                   PasswordEncoder passwordEncoder) {
        this.customerRepository = customerRepository;
        this.otpRepository = otpRepository;
        this.consentRepository = consentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public Map<String, Object> execute(RegisterRequest request, String ipAddress) {
        if (customerRepository.existsByEmail(request.email())) {
            throw new IllegalStateException("EMAIL_ALREADY_EXISTS");
        }

        String loyaltyId = generateLoyaltyId();
        String passwordHash = passwordEncoder.encode(request.password());

        Customer customer = new Customer(
                request.email(), request.phone(), request.name(), passwordHash, loyaltyId
        );
        customer = customerRepository.save(customer);

        // Generate and store OTP
        String otpPlain = generateOtp();
        String otpHash = passwordEncoder.encode(otpPlain);
        OtpCode otp = new OtpCode(
                customer.getId(), otpHash,
                OtpCode.OtpPurpose.REGISTRATION,
                Instant.now().plusSeconds(300)
        );
        otpRepository.save(otp);

        // Record consent
        consentRepository.save(new Consent(
                customer.getId(), "terms_and_conditions",
                request.termsAndConditionsVersion(), true, ipAddress
        ));

        return Map.of(
                "customerId", customer.getId(),
                "status", "pending_verification",
                "message", "Verification code sent to your email"
        );
    }

    private String generateOtp() {
        return String.format("%06d", secureRandom.nextInt(1_000_000));
    }

    private String generateLoyaltyId() {
        return String.format("%010d", secureRandom.nextLong(1_000_000_000L, 9_999_999_999L));
    }
}
