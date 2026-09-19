package com.tokenization.mask.controller;

import com.tokenization.mask.dto.TokenRequest;
import com.tokenization.mask.dto.TokenResponse;
import com.tokenization.mask.service.TokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Tokenizes and decodes an arbitrary JSON payload (object, array, or scalar) as-is,
 * with no schema - whatever is sent to /generate comes back unchanged from /decode.
 */
@RestController
@RequestMapping("/api/token")
public class TokenController {

    private static final Logger log = LoggerFactory.getLogger(TokenController.class);

    private final TokenService tokenService;

    public TokenController(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @PostMapping("/generate")
    public TokenResponse generate(@RequestBody Object payload) {
        log.info("Received /generate request, payloadType={}", payload.getClass().getSimpleName());
        TokenResponse response = new TokenResponse(tokenService.generate(payload));
        log.info("Generated token for /generate request");
        return response;
    }

    @PostMapping("/decode")
    public Object decode(@RequestBody TokenRequest request) {
        log.info("Received /decode request");
        Object payload = tokenService.decode(request.token());
        log.info("Decoded token for /decode request");
        return payload;
    }
}
