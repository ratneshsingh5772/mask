package com.tokenization.mask.service;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TokenServiceTest {

    private static final String SECRET = "unit-test-secret-key-at-least-32-bytes-long";

    @Test
    void aFlatObjectRoundTripsThroughGenerateAndDecode() {
        TokenService tokenService = new TokenService(SECRET, 60_000);
        Map<String, Object> payload = Map.of("userId", 101, "role", "ADMIN");

        String token = tokenService.generate(payload);

        assertThat(tokenService.decode(token)).isEqualTo(payload);
    }

    @Test
    void anArrayRoundTripsThroughGenerateAndDecode() {
        TokenService tokenService = new TokenService(SECRET, 60_000);
        List<Object> payload = List.of(1, 2, "three");

        String token = tokenService.generate(payload);

        assertThat(tokenService.decode(token)).isEqualTo(payload);
    }

    @Test
    void generateRejectsANullPayload() {
        TokenService tokenService = new TokenService(SECRET, 60_000);

        assertThatThrownBy(() -> tokenService.generate(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void decodeRejectsABlankToken() {
        TokenService tokenService = new TokenService(SECRET, 60_000);

        assertThatThrownBy(() -> tokenService.decode(" "))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void decodeRejectsATamperedToken() {
        TokenService tokenService = new TokenService(SECRET, 60_000);
        String token = tokenService.generate(Map.of("userId", 101));
        String tampered = token.substring(0, token.length() - 1) + (token.endsWith("A") ? "B" : "A");

        assertThatThrownBy(() -> tokenService.decode(tampered))
                .isInstanceOf(SignatureException.class);
    }

    @Test
    void decodeRejectsATokenSignedWithADifferentSecret() {
        TokenService issuer = new TokenService(SECRET, 60_000);
        TokenService verifier = new TokenService("a-completely-different-secret-key-value", 60_000);
        String token = issuer.generate(Map.of("userId", 101));

        assertThatThrownBy(() -> verifier.decode(token))
                .isInstanceOf(SignatureException.class);
    }

    @Test
    void decodeRejectsAnExpiredToken() {
        TokenService tokenService = new TokenService(SECRET, 0);
        String token = tokenService.generate(Map.of("userId", 101));

        assertThatThrownBy(() -> tokenService.decode(token))
                .isInstanceOf(ExpiredJwtException.class);
    }
}
