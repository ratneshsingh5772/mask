package com.tokenization.mask.controller;

import com.tokenization.mask.dto.TokenRequest;
import com.tokenization.mask.dto.TokenResponse;
import com.tokenization.mask.dto.UserPayload;
import com.tokenization.mask.security.JwtService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

/**
 * Generates and decodes the JWTs that carry request payloads for the rest of this API.
 * Unlike {@link UserController}, these endpoints still take a normal JSON body -
 * they are how a payload gets turned into a token in the first place.
 */
@RestController
@RequestMapping("/api/token")
public class TokenController {

    private final JwtService jwtService;

    public TokenController(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @PostMapping("/generate")
    public ResponseEntity<TokenResponse> generate(@Valid @RequestBody UserPayload payload) {
        String token = jwtService.issue(payload);
        Instant expiresAt = Instant.now().plus(jwtService.getTokenTtl());
        return ResponseEntity.ok(new TokenResponse(token, expiresAt));
    }

    @PostMapping("/decode")
    public ResponseEntity<UserPayload> decode(@Valid @RequestBody TokenRequest request) {
        return ResponseEntity.ok(jwtService.parse(request.token(), UserPayload.class));
    }
}
