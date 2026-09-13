package com.tokenization.mask.controller;

import com.tokenization.mask.dto.UserPayload;
import com.tokenization.mask.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Test
    void createReadsThePayloadEntirelyFromTheBearerTokenNoJsonBody() throws Exception {
        UserPayload payload = new UserPayload(101L, "Ratnesh", "ADMIN");
        String token = jwtService.issue(payload);

        mockMvc.perform(post("/api/v1/users")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(101))
                .andExpect(jsonPath("$.name").value("Ratnesh"))
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    void createWithoutAnAuthorizationHeaderIsUnauthorized() throws Exception {
        mockMvc.perform(post("/api/v1/users"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createWithATamperedTokenIsUnauthorized() throws Exception {
        String token = jwtService.issue(new UserPayload(101L, "Ratnesh", "ADMIN"));
        String tampered = token.substring(0, token.length() - 1) + (token.endsWith("A") ? "B" : "A");

        mockMvc.perform(post("/api/v1/users")
                        .header("Authorization", "Bearer " + tampered))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createWithAnExpiredTokenIsUnauthorized() throws Exception {
        JwtService expiredIssuer = new JwtService("Gy5yK/3cjXD529NdyhP+v1VK8N92JItc/YqAxLFFk5c=", 0);
        String expiredToken = expiredIssuer.issue(new UserPayload(101L, "Ratnesh", "ADMIN"));

        mockMvc.perform(post("/api/v1/users")
                        .header("Authorization", "Bearer " + expiredToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getReturnsAPreviouslyCreatedUser() throws Exception {
        String token = jwtService.issue(new UserPayload(303L, "Meera", "USER"));
        mockMvc.perform(post("/api/v1/users").header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/users/303").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(303))
                .andExpect(jsonPath("$.name").value("Meera"));
    }

    @Test
    void getAnUnknownUserIsNotFound() throws Exception {
        String token = jwtService.issue(new UserPayload(9999L, "Nobody", "USER"));

        mockMvc.perform(get("/api/v1/users/424242").header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void getWithoutAnAuthorizationHeaderIsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/users/303"))
                .andExpect(status().isUnauthorized());
    }
}
