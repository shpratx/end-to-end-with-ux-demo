package uk.co.Dunelm.loyalty.auth.application.command;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import uk.co.Dunelm.loyalty.auth.application.dto.AuthTokenResponse;
import uk.co.Dunelm.loyalty.auth.application.dto.LoginRequest;
import uk.co.Dunelm.loyalty.auth.domain.model.Customer;
import uk.co.Dunelm.loyalty.auth.domain.port.CustomerRepository;
import uk.co.Dunelm.loyalty.auth.domain.port.TokenRepository;
import uk.co.Dunelm.loyalty.auth.infrastructure.config.JwtService;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoginCommandTest {

    @Mock private CustomerRepository customerRepository;
    @Mock private TokenRepository tokenRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;

    private LoginCommand command;

    @BeforeEach
    void setUp() {
        command = new LoginCommand(customerRepository, tokenRepository, passwordEncoder, jwtService);
    }

    private Customer activeCustomer() throws Exception {
        Customer c = new Customer("jane@example.com", "+447700900123", "Jane", "$2a$12$hash", "1234567890");
        var idField = Customer.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(c, UUID.randomUUID());
        var statusField = Customer.class.getDeclaredField("status");
        statusField.setAccessible(true);
        statusField.set(c, Customer.CustomerStatus.ACTIVE);
        return c;
    }

    @Test
    void execute_validCredentials_returnsTokens() throws Exception {
        Customer customer = activeCustomer();
        when(customerRepository.findByEmail("jane@example.com")).thenReturn(Optional.of(customer));
        when(passwordEncoder.matches("password123!", "$2a$12$hash")).thenReturn(true);
        when(jwtService.generateAccessToken(any())).thenReturn("access-token");
        when(jwtService.getAccessTokenExpiry()).thenReturn(900);
        when(jwtService.getRefreshTokenExpiry()).thenReturn(2592000);
        when(tokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        AuthTokenResponse response = command.execute(new LoginRequest("jane@example.com", "password123!"));

        assertEquals("access-token", response.accessToken());
        assertNotNull(response.refreshToken());
        assertEquals(900, response.expiresIn());
        assertEquals("Bearer", response.tokenType());
    }

    @Test
    void execute_invalidEmail_throwsInvalidCredentials() {
        when(customerRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        var ex = assertThrows(IllegalArgumentException.class,
                () -> command.execute(new LoginRequest("unknown@example.com", "pass")));
        assertEquals("INVALID_CREDENTIALS", ex.getMessage());
    }

    @Test
    void execute_wrongPassword_incrementsFailedAttempts() throws Exception {
        Customer customer = activeCustomer();
        when(customerRepository.findByEmail("jane@example.com")).thenReturn(Optional.of(customer));
        when(passwordEncoder.matches("wrong", "$2a$12$hash")).thenReturn(false);
        when(customerRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        assertThrows(IllegalArgumentException.class,
                () -> command.execute(new LoginRequest("jane@example.com", "wrong")));

        assertEquals(1, customer.getFailedLoginAttempts());
        verify(customerRepository).save(customer);
    }

    @Test
    void execute_lockedAccount_throwsAccountLocked() throws Exception {
        Customer customer = activeCustomer();
        // Lock the account by simulating 5 failed attempts
        for (int i = 0; i < 5; i++) customer.recordFailedLogin();
        when(customerRepository.findByEmail("jane@example.com")).thenReturn(Optional.of(customer));

        var ex = assertThrows(IllegalStateException.class,
                () -> command.execute(new LoginRequest("jane@example.com", "pass")));
        assertEquals("ACCOUNT_LOCKED", ex.getMessage());
    }

    @Test
    void execute_pendingVerification_throwsNotVerified() throws Exception {
        Customer customer = new Customer("jane@example.com", "+447700900123", "Jane", "$2a$12$hash", "1234567890");
        var idField = Customer.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(customer, UUID.randomUUID());
        when(customerRepository.findByEmail("jane@example.com")).thenReturn(Optional.of(customer));

        var ex = assertThrows(IllegalStateException.class,
                () -> command.execute(new LoginRequest("jane@example.com", "pass")));
        assertEquals("ACCOUNT_NOT_VERIFIED", ex.getMessage());
    }

    @Test
    void execute_successfulLogin_resetsFailedAttempts() throws Exception {
        Customer customer = activeCustomer();
        customer.recordFailedLogin();
        customer.recordFailedLogin();
        when(customerRepository.findByEmail("jane@example.com")).thenReturn(Optional.of(customer));
        when(passwordEncoder.matches("correct", "$2a$12$hash")).thenReturn(true);
        when(jwtService.generateAccessToken(any())).thenReturn("token");
        when(jwtService.getAccessTokenExpiry()).thenReturn(900);
        when(jwtService.getRefreshTokenExpiry()).thenReturn(2592000);
        when(tokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        command.execute(new LoginRequest("jane@example.com", "correct"));

        assertEquals(0, customer.getFailedLoginAttempts());
        assertNull(customer.getLockedUntil());
    }
}
