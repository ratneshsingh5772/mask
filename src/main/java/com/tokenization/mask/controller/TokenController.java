package com.tokenization.mask.controller;

import com.tokenization.mask.dto.TokenRequest;
import com.tokenization.mask.dto.TokenResponse;
import com.tokenization.mask.service.TokenService;
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

    private final TokenService tokenService;

    public TokenController(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @PostMapping("/generate")
    public TokenResponse generate(@RequestBody Object payload) {
        return new TokenResponse(tokenService.generate(payload));
    }

    @PostMapping("/decode")
    public Object decode(@RequestBody TokenRequest request) {
        return tokenService.decode(request.token());
    }
}
