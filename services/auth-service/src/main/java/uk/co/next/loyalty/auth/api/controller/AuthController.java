package uk.co.next.loyalty.auth.api.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.co.next.loyalty.auth.application.command.*;
import uk.co.next.loyalty.auth.application.dto.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final RegisterCustomerCommand registerCommand;
    private final VerifyOtpCommand verifyOtpCommand;
    private final ResendOtpCommand resendOtpCommand;
    private final LoginCommand loginCommand;
    private final SocialLoginCommand socialLoginCommand;
    private final RefreshTokenCommand refreshTokenCommand;
    private final LogoutCommand logoutCommand;
    private final RequestPasswordResetCommand requestPasswordResetCommand;
    private final ConfirmPasswordResetCommand confirmPasswordResetCommand;

    public AuthController(RegisterCustomerCommand registerCommand,
                          VerifyOtpCommand verifyOtpCommand,
                          ResendOtpCommand resendOtpCommand,
                          LoginCommand loginCommand,
                          SocialLoginCommand socialLoginCommand,
                          RefreshTokenCommand refreshTokenCommand,
                          LogoutCommand logoutCommand,
                          RequestPasswordResetCommand requestPasswordResetCommand,
                          ConfirmPasswordResetCommand confirmPasswordResetCommand) {
        this.registerCommand = registerCommand;
        this.verifyOtpCommand = verifyOtpCommand;
        this.resendOtpCommand = resendOtpCommand;
        this.loginCommand = loginCommand;
        this.socialLoginCommand = socialLoginCommand;
        this.refreshTokenCommand = refreshTokenCommand;
        this.logoutCommand = logoutCommand;
        this.requestPasswordResetCommand = requestPasswordResetCommand;
        this.confirmPasswordResetCommand = confirmPasswordResetCommand;
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(
            @Valid @RequestBody RegisterRequest request, HttpServletRequest httpRequest) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(registerCommand.execute(request, httpRequest.getRemoteAddr()));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<Map<String, Object>> verifyOtp(@Valid @RequestBody OtpVerifyRequest request) {
        return ResponseEntity.ok(verifyOtpCommand.execute(request.customerId(), request.otpCode()));
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<Map<String, Object>> resendOtp(@RequestBody Map<String, UUID> body) {
        return ResponseEntity.ok(resendOtpCommand.execute(body.get("customerId")));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthTokenResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(loginCommand.execute(request));
    }

    @PostMapping("/login/social")
    public ResponseEntity<Map<String, Object>> socialLogin(@RequestBody Map<String, String> body) {
        return ResponseEntity.ok(socialLoginCommand.execute(body.get("provider"), body.get("idToken")));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthTokenResponse> refresh(@RequestBody Map<String, String> body) {
        return ResponseEntity.ok(refreshTokenCommand.execute(body.get("refreshToken")));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody Map<String, String> body) {
        logoutCommand.execute(body.get("refreshToken"));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/reset-password/request")
    public ResponseEntity<Map<String, String>> requestReset(@RequestBody Map<String, String> body) {
        return ResponseEntity.ok(requestPasswordResetCommand.execute(body.get("email")));
    }

    @PostMapping("/reset-password/confirm")
    public ResponseEntity<Map<String, Boolean>> confirmReset(@RequestBody Map<String, String> body) {
        // In production, customerId would be derived from the token
        return ResponseEntity.ok(confirmPasswordResetCommand.execute(
                body.get("token"), body.get("newPassword"),
                UUID.fromString(body.get("customerId"))
        ));
    }
}
