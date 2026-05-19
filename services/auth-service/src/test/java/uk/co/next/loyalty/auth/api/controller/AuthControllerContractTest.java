package uk.co.Dunelm.loyalty.auth.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import uk.co.Dunelm.loyalty.auth.application.command.*;
import uk.co.Dunelm.loyalty.auth.application.dto.AuthTokenResponse;
import uk.co.Dunelm.loyalty.auth.application.dto.LoginRequest;
import uk.co.Dunelm.loyalty.auth.application.dto.RegisterRequest;
import uk.co.Dunelm.loyalty.auth.infrastructure.config.JwtAuthFilter;
import uk.co.Dunelm.loyalty.auth.infrastructure.config.JwtService;

import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerContractTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private RegisterCustomerCommand registerCommand;
    @MockBean private VerifyOtpCommand verifyOtpCommand;
    @MockBean private ResendOtpCommand resendOtpCommand;
    @MockBean private LoginCommand loginCommand;
    @MockBean private SocialLoginCommand socialLoginCommand;
    @MockBean private RefreshTokenCommand refreshTokenCommand;
    @MockBean private LogoutCommand logoutCommand;
    @MockBean private RequestPasswordResetCommand requestPasswordResetCommand;
    @MockBean private ConfirmPasswordResetCommand confirmPasswordResetCommand;
    @MockBean private JwtService jwtService;
    @MockBean private JwtAuthFilter jwtAuthFilter;

    @Test
    void register_returns201WithCustomerId() throws Exception {
        UUID customerId = UUID.randomUUID();
        when(registerCommand.execute(any(), any())).thenReturn(Map.of(
                "customerId", customerId,
                "status", "pending_verification",
                "message", "Verification code sent to your email"
        ));

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RegisterRequest(
                                "Jane", "jane@example.com", "+447700900123", "SecureP@ss2026!", "2.1"
                        ))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.customerId").value(customerId.toString()))
                .andExpect(jsonPath("$.status").value("pending_verification"));
    }

    @Test
    void login_returns200WithTokens() throws Exception {
        when(loginCommand.execute(any())).thenReturn(
                new AuthTokenResponse("access-token", "refresh-token", 900)
        );

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest("jane@example.com", "SecureP@ss2026!"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"))
                .andExpect(jsonPath("$.expiresIn").value(900))
                .andExpect(jsonPath("$.tokenType").value("Bearer"));
    }

    @Test
    void logout_returns204() throws Exception {
        mockMvc.perform(post("/api/v1/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"some-token\"}"))
                .andExpect(status().isNoContent());
    }

    @Test
    void resetPasswordRequest_returns200() throws Exception {
        when(requestPasswordResetCommand.execute(any())).thenReturn(
                Map.of("message", "If an account exists with this email, a reset link has been sent")
        );

        mockMvc.perform(post("/api/v1/auth/reset-password/request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"jane@example.com\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").exists());
    }
}
