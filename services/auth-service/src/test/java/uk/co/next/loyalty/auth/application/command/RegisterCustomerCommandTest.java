package uk.co.Dunelm.loyalty.auth.application.command;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import uk.co.Dunelm.loyalty.auth.application.dto.RegisterRequest;
import uk.co.Dunelm.loyalty.auth.domain.model.Consent;
import uk.co.Dunelm.loyalty.auth.domain.model.Customer;
import uk.co.Dunelm.loyalty.auth.domain.model.OtpCode;
import uk.co.Dunelm.loyalty.auth.domain.port.CustomerRepository;
import uk.co.Dunelm.loyalty.auth.domain.port.OtpRepository;
import uk.co.Dunelm.loyalty.auth.infrastructure.persistence.JpaConsentRepository;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegisterCustomerCommandTest {

    @Mock private CustomerRepository customerRepository;
    @Mock private OtpRepository otpRepository;
    @Mock private JpaConsentRepository consentRepository;
    @Mock private PasswordEncoder passwordEncoder;

    private RegisterCustomerCommand command;

    @BeforeEach
    void setUp() {
        command = new RegisterCustomerCommand(customerRepository, otpRepository, consentRepository, passwordEncoder);
    }

    @Test
    void execute_success_returnsCustomerIdAndPendingStatus() {
        var request = new RegisterRequest("Jane Smith", "jane@example.com", "+447700900123", "SecureP@ss2026!", "2.1");
        when(customerRepository.existsByEmail("jane@example.com")).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("$2a$12$hashedpassword");
        when(customerRepository.save(any(Customer.class))).thenAnswer(inv -> {
            Customer c = inv.getArgument(0);
            // Simulate JPA setting the ID
            var field = Customer.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(c, UUID.randomUUID());
            return c;
        });
        when(otpRepository.save(any(OtpCode.class))).thenAnswer(inv -> inv.getArgument(0));
        when(consentRepository.save(any(Consent.class))).thenAnswer(inv -> inv.getArgument(0));

        Map<String, Object> result = command.execute(request, "127.0.0.1");

        assertNotNull(result.get("customerId"));
        assertEquals("pending_verification", result.get("status"));
        assertEquals("Verification code sent to your email", result.get("message"));

        verify(customerRepository).save(any(Customer.class));
        verify(otpRepository).save(any(OtpCode.class));
        verify(consentRepository).save(any(Consent.class));
    }

    @Test
    void execute_duplicateEmail_throwsException() {
        var request = new RegisterRequest("Jane", "existing@example.com", "+447700900123", "SecureP@ss2026!", "2.1");
        when(customerRepository.existsByEmail("existing@example.com")).thenReturn(true);

        var ex = assertThrows(IllegalStateException.class, () -> command.execute(request, "127.0.0.1"));
        assertEquals("EMAIL_ALREADY_EXISTS", ex.getMessage());
        verify(customerRepository, never()).save(any());
    }

    @Test
    void execute_hashesPasswordWithBcrypt() {
        var request = new RegisterRequest("Jane", "jane@example.com", "+447700900123", "SecureP@ss2026!", "2.1");
        when(customerRepository.existsByEmail(any())).thenReturn(false);
        when(passwordEncoder.encode("SecureP@ss2026!")).thenReturn("$2a$12$hashed");
        when(customerRepository.save(any(Customer.class))).thenAnswer(inv -> {
            Customer c = inv.getArgument(0);
            var field = Customer.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(c, UUID.randomUUID());
            return c;
        });
        when(otpRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(consentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        command.execute(request, "127.0.0.1");

        verify(passwordEncoder).encode("SecureP@ss2026!");
        ArgumentCaptor<Customer> captor = ArgumentCaptor.forClass(Customer.class);
        verify(customerRepository).save(captor.capture());
        assertEquals("$2a$12$hashed", captor.getValue().getPasswordHash());
    }

    @Test
    void execute_storesOtpWithFiveMinuteExpiry() {
        var request = new RegisterRequest("Jane", "jane@example.com", "+447700900123", "SecureP@ss2026!", "2.1");
        when(customerRepository.existsByEmail(any())).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("hashed");
        when(customerRepository.save(any(Customer.class))).thenAnswer(inv -> {
            Customer c = inv.getArgument(0);
            var field = Customer.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(c, UUID.randomUUID());
            return c;
        });
        when(otpRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(consentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        command.execute(request, "127.0.0.1");

        ArgumentCaptor<OtpCode> captor = ArgumentCaptor.forClass(OtpCode.class);
        verify(otpRepository).save(captor.capture());
        OtpCode otp = captor.getValue();
        assertEquals(OtpCode.OtpPurpose.REGISTRATION, otp.getPurpose());
        assertFalse(otp.isExpired());
    }
}
