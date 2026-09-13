package com.tokenization.mask.security;

import com.tokenization.mask.dto.UserPayload;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private static final String SECRET = "Gy5yK/3cjXD529NdyhP+v1VK8N92JItc/YqAxLFFk5c=";

    @Test
    void issueThenParseRoundTripsThePayload() {
        JwtService jwtService = new JwtService(SECRET, 15);
        UserPayload payload = new UserPayload(101L, "Ratnesh", "ADMIN");

        String token = jwtService.issue(payload);
        UserPayload decoded = jwtService.parse(token, UserPayload.class);

        assertThat(decoded).isEqualTo(payload);
    }

    @Test
    void parseRejectsATamperedToken() {
        JwtService jwtService = new JwtService(SECRET, 15);
        String token = jwtService.issue(new UserPayload(101L, "Ratnesh", "ADMIN"));
        String tampered = token.substring(0, token.length() - 1) + (token.endsWith("A") ? "B" : "A");

        assertThatThrownBy(() -> jwtService.parse(tampered, UserPayload.class))
                .isInstanceOf(SignatureException.class);
    }

    @Test
    void parseRejectsATokenSignedWithADifferentSecret() {
        JwtService issuer = new JwtService(SECRET, 15);
        JwtService verifier = new JwtService("bm90LXRoZS1zYW1lLXNlY3JldC1hdC1hbGwtMzJieXRlcw==", 15);
        String token = issuer.issue(new UserPayload(101L, "Ratnesh", "ADMIN"));

        assertThatThrownBy(() -> verifier.parse(token, UserPayload.class))
                .isInstanceOf(SignatureException.class);
    }

    @Test
    void parseRejectsAnExpiredToken() {
        JwtService jwtService = new JwtService(SECRET, 0);
        String token = jwtService.issue(new UserPayload(101L, "Ratnesh", "ADMIN"));

        assertThatThrownBy(() -> jwtService.parse(token, UserPayload.class))
                .isInstanceOf(ExpiredJwtException.class);
    }
}
