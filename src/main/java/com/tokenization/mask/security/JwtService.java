package com.tokenization.mask.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

/**
 * Signs and verifies the JWS tokens that carry request payloads for this API.
 * The token's claims ARE the request body - there is no separate JSON payload.
 */
@Service
public class JwtService {

    private static final String PAYLOAD_SUBJECT = "tokenized-request-payload";

    private final SecretKey signingKey;
    private final Duration tokenTtl;
    private final ObjectMapper claimsMapper;

    public JwtService(
            @Value("${jwt.secret}") String base64Secret,
            @Value("${jwt.expiration-minutes:15}") long expirationMinutes) {
        this.signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(base64Secret));
        this.tokenTtl = Duration.ofMinutes(expirationMinutes);
        this.claimsMapper = JsonMapper.builder()
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .build();
    }

    public Duration getTokenTtl() {
        return tokenTtl;
    }

    /** Serializes {@code payload} into JWT claims and signs it as a compact JWS string. */
    public <T> String issue(T payload) {
        Instant now = Instant.now();
        Map<String, Object> claims = claimsMapper.convertValue(payload, new TypeReference<Map<String, Object>>() {
        });
        return Jwts.builder()
                .subject(PAYLOAD_SUBJECT)
                .claims(claims)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(tokenTtl)))
                .signWith(signingKey)
                .compact();
    }

    /** Verifies the token's signature and expiry, returning its raw claims. */
    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /** Verifies the token and materializes its claims as {@code targetType}. */
    public <T> T parse(String token, Class<T> targetType) {
        return claimsMapper.convertValue(parseClaims(token), targetType);
    }
}
