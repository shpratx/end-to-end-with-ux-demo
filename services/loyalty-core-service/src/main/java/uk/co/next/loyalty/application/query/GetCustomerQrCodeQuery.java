package uk.co.Dunelm.loyalty.application.query;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

@Service
public class GetCustomerQrCodeQuery {

    @Value("${loyalty.qr.secret:0123456789abcdef}")
    private String qrSecret;

    public record Response(String qrPayload, Instant expiresAt, int refreshInSeconds) {}

    public Response execute(UUID customerId) {
        Instant expiresAt = Instant.now().plusSeconds(60);
        String payload = customerId.toString() + "|" + expiresAt.toEpochMilli();
        String encrypted = encrypt(payload);
        return new Response(encrypted, expiresAt, 60);
    }

    private String encrypt(String data) {
        try {
            byte[] key = qrSecret.getBytes(StandardCharsets.UTF_8);
            SecretKeySpec spec = new SecretKeySpec(java.util.Arrays.copyOf(key, 16), "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, spec);
            return Base64.getUrlEncoder().encodeToString(cipher.doFinal(data.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new RuntimeException("QR encryption failed", e);
        }
    }
}
