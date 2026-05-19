package uk.co.Dunelm.loyalty.auth.application.command;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.Dunelm.loyalty.auth.domain.model.RefreshToken;
import uk.co.Dunelm.loyalty.auth.domain.port.TokenRepository;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

@Service
public class LogoutCommand {

    private final TokenRepository tokenRepository;

    public LogoutCommand(TokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    @Transactional
    public void execute(String refreshTokenRaw) {
        if (refreshTokenRaw == null || refreshTokenRaw.isBlank()) return;
        String hash = sha256(refreshTokenRaw);
        tokenRepository.findByTokenHash(hash).ifPresent(token -> {
            token.revoke();
            tokenRepository.save(token);
        });
    }

    private String sha256(String input) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) { throw new RuntimeException(e); }
    }
}
