package com.tokenization.mask.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class TokenControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void aFlatJsonObjectRoundTripsThroughGenerateAndDecode() throws Exception {
        Map<String, Object> payload = Map.of("userId", 101, "name", "Ratnesh", "role", "ADMIN", "test", "test");

        String token = generateToken(payload);

        mockMvc.perform(post("/api/token/decode")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of("token", token))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(101))
                .andExpect(jsonPath("$.name").value("Ratnesh"))
                .andExpect(jsonPath("$.role").value("ADMIN"))
                .andExpect(jsonPath("$.test").value("test"));
    }

    @Test
    void aJsonArrayRoundTripsThroughGenerateAndDecode() throws Exception {
        List<Object> payload = List.of(1, 2, 3, "x");

        String token = generateToken(payload);

        mockMvc.perform(post("/api/token/decode")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of("token", token))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value(1))
                .andExpect(jsonPath("$[3]").value("x"));
    }

    @Test
    void generateRejectsAnEmptyBody() throws Exception {
        mockMvc.perform(post("/api/token/generate")
                        .contentType("application/json")
                        .content(""))
                .andExpect(status().isBadRequest());
    }

    @Test
    void decodeRejectsATamperedToken() throws Exception {
        String token = generateToken(Map.of("userId", 101));
        String tampered = token.substring(0, token.length() - 1) + (token.endsWith("A") ? "B" : "A");

        mockMvc.perform(post("/api/token/decode")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of("token", tampered))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void decodeRejectsABlankToken() throws Exception {
        mockMvc.perform(post("/api/token/decode")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of("token", ""))))
                .andExpect(status().isBadRequest());
    }

    private String generateToken(Object payload) throws Exception {
        String responseBody = mockMvc.perform(post("/api/token/generate")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readValue(responseBody, Map.class).get("token").toString();
    }
}
