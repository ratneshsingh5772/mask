package com.tokenization.mask.controller;

import com.tokenization.mask.dto.UserPayload;
import com.tokenization.mask.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UserTokenControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtService jwtService;

    @Test
    void generateReturnsASignedJwtForAValidPayload() throws Exception {
        UserPayload payload = new UserPayload(101L, "Ratnesh", "ADMIN");

        mockMvc.perform(post("/api/v1/users/token/generate")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(matchesPattern("^[A-Za-z0-9_-]+\\.[A-Za-z0-9_-]+\\.[A-Za-z0-9_-]+$")))
                .andExpect(jsonPath("$.expiresAt").exists());
    }

    @Test
    void generateRejectsAnInvalidPayload() throws Exception {
        String invalidJson = "{\"userId\": 101, \"name\": \"\", \"role\": \"SUPERUSER\"}";

        mockMvc.perform(post("/api/v1/users/token/generate")
                        .contentType("application/json")
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void decodeReturnsTheOriginalPayloadForAValidToken() throws Exception {
        UserPayload payload = new UserPayload(202L, "Asha", "MANAGER");
        String token = jwtService.issue(payload);

        mockMvc.perform(post("/api/v1/users/token/decode")
                        .contentType("application/json")
                        .content("{\"token\": \"" + token + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(202))
                .andExpect(jsonPath("$.name").value("Asha"))
                .andExpect(jsonPath("$.role").value("MANAGER"));
    }

    @Test
    void decodeRejectsATamperedToken() throws Exception {
        String token = jwtService.issue(new UserPayload(202L, "Asha", "MANAGER"));
        String tampered = token.substring(0, token.length() - 1) + (token.endsWith("A") ? "B" : "A");

        mockMvc.perform(post("/api/v1/users/token/decode")
                        .contentType("application/json")
                        .content("{\"token\": \"" + tampered + "\"}"))
                .andExpect(status().isUnauthorized());
    }
}
