package com.example.Vi_vu_Viet.Security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tools.jackson.databind.json.JsonMapper;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class JwtService {

    private static final long ACCESS_TOKEN_SECONDS = 86_400;
    private static final String JWT_HEADER = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";

    private final JsonMapper jsonMapper;
    private final byte[] secretKey;

    public JwtService(
            JsonMapper jsonMapper,
            @Value("${app.auth.jwt-secret:}") String configuredSecret
    ) {
        this.jsonMapper = jsonMapper;

        if (configuredSecret != null && !configuredSecret.isBlank() && configuredSecret.length() < 32) {
            throw new IllegalArgumentException("AUTH_JWT_SECRET must contain at least 32 characters");
        }

        if (configuredSecret != null && !configuredSecret.isBlank()) {
            this.secretKey = configuredSecret.getBytes(StandardCharsets.UTF_8);
        } else {
            this.secretKey = new byte[32];
            new SecureRandom().nextBytes(this.secretKey);
        }
    }

    public String createAccessToken(UUID userId, UUID sessionId) {
        try {
            long issuedAt = Instant.now().getEpochSecond();
            Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();
            String header = encoder.encodeToString(JWT_HEADER.getBytes(StandardCharsets.UTF_8));
            String payload = encoder.encodeToString(jsonMapper.writeValueAsBytes(Map.of(
                    "sub", userId.toString(),
                    "sid", sessionId.toString(),
                    "iat", issuedAt,
                    "exp", issuedAt + ACCESS_TOKEN_SECONDS
            )));
            String unsignedToken = header + "." + payload;
            return unsignedToken + "." + sign(unsignedToken);
        } catch (Exception exception) {
            throw new IllegalStateException("Could not create access token", exception);
        }
    }

    public Optional<TokenClaims> getTokenClaims(String token) {
        try {
            String[] tokenParts = token.split("\\.");
            if (tokenParts.length != 3) {
                return Optional.empty();
            }

            String unsignedToken = tokenParts[0] + "." + tokenParts[1];
            byte[] expectedSignature = Base64.getUrlDecoder().decode(sign(unsignedToken));
            byte[] actualSignature = Base64.getUrlDecoder().decode(tokenParts[2]);
            if (!MessageDigest.isEqual(expectedSignature, actualSignature)) {
                return Optional.empty();
            }

            String header = new String(Base64.getUrlDecoder().decode(tokenParts[0]), StandardCharsets.UTF_8);
            if (!JWT_HEADER.equals(header)) {
                return Optional.empty();
            }

            Map<?, ?> claims = jsonMapper.readValue(
                    Base64.getUrlDecoder().decode(tokenParts[1]),
                    Map.class
            );
            long expiresAt = ((Number) claims.get("exp")).longValue();
            if (expiresAt <= Instant.now().getEpochSecond()) {
                return Optional.empty();
            }

            return Optional.of(new TokenClaims(
                    UUID.fromString((String) claims.get("sub")),
                    UUID.fromString((String) claims.get("sid")),
                    Instant.ofEpochSecond(expiresAt)
            ));
        } catch (Exception exception) {
            return Optional.empty();
        }
    }

    public long getAccessTokenSeconds() {
        return ACCESS_TOKEN_SECONDS;
    }

    public record TokenClaims(UUID userId, UUID sessionId, Instant expiresAt) {
    }

    private String sign(String value) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secretKey, "HmacSHA256"));
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));
    }
}
