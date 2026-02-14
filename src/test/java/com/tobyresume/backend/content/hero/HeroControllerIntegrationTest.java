package com.tobyresume.backend.content.hero;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tobyresume.backend.content.hero.dto.HeroRequest;
import com.tobyresume.backend.content.hero.dto.HeroResponse;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Map;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@TestPropertySource(properties = {
        "app.cors.allowed-origins=http://localhost:3000,http://localhost:3001",
        "app.security.jwt.secret=test-jwt-secret-at-least-32-characters-long",
        "app.security.jwt.expiration-ms=3600000",
        "app.security.oauth2.redirect-uri=http://localhost:3000/auth/callback"
})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class HeroControllerIntegrationTest {

    @Container
    static MongoDBContainer mongo = new MongoDBContainer("mongo:7");

    @DynamicPropertySource
    static void mongoProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongo::getReplicaSetUrl);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @Order(1)
    void getHero_unauthorized_withoutAuth() throws Exception {
        mockMvc.perform(get("/api/v1/hero"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(2)
    @WithMockUser
    void getHero_returnsNullData_whenNoDraft() throws Exception {
        mockMvc.perform(get("/api/v1/hero"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @Order(3)
    @WithMockUser
    void getHero_returnsData_whenDraftExists() throws Exception {
        HeroRequest request = new HeroRequest();
        request.setTagline(Map.of("en", "Developer"));
        request.setFullName(Map.of("en", "Toby"));
        mockMvc.perform(put("/api/v1/hero")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/hero"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.tagline.en").value("Developer"))
                .andExpect(jsonPath("$.data.fullName.en").value("Toby"));
    }

    @Test
    @Order(4)
    void putHero_unauthorized_withoutAuth() throws Exception {
        HeroRequest request = new HeroRequest();
        request.setTagline(Map.of("en", "Dev"));

        mockMvc.perform(put("/api/v1/hero")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(5)
    @WithMockUser
    void putHero_returns200_andSaves() throws Exception {
        HeroRequest request = new HeroRequest();
        request.setTagline(Map.of("en", "Full Stack Developer"));
        request.setFullName(Map.of("en", "Toby Nguyen"));

        mockMvc.perform(put("/api/v1/hero")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.tagline.en").value("Full Stack Developer"))
                .andExpect(jsonPath("$.data.fullName.en").value("Toby Nguyen"));
    }

    @Test
    @Order(6)
    @WithMockUser
    void putHero_returns400_whenValidationFails() throws Exception {
        // Invalid: tagline value exceeds 500 chars
        HeroRequest request = new HeroRequest();
        request.setTagline(Map.of("en", "x".repeat(501)));

        mockMvc.perform(put("/api/v1/hero")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
    }
}
