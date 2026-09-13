package com.tokenization.mask.controller;

import com.tokenization.mask.service.TokenService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/token")
public class TokenController {

    private final TokenService tokenService;

    public TokenController(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @PostMapping("/generate")
    public Map<String, String> generate(@RequestBody Object payload) {
        return Map.of("token", tokenService.generate(payload));
    }

    @PostMapping("/decode")
    public Object decode(@RequestBody Map<String, String> body) {
        return tokenService.decode(body.get("token"));
    }
}
