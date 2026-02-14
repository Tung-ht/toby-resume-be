package com.tobyresume.backend.preview;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@TestPropertySource(properties = {
        "app.security.jwt.secret=test-jwt-secret-at-least-32-characters-long",
        "app.security.oauth2.redirect-uri=http://localhost:3000/auth/callback"
})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PreviewControllerIntegrationTest {

    @Container
    static MongoDBContainer mongo = new MongoDBContainer("mongo:7");

    @DynamicPropertySource
    static void mongoProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongo::getReplicaSetUrl);
    }

    @Autowired
    private MockMvc mockMvc;

    @Test
    @Order(1)
    void getPreview_unauthorized_withoutAuth() throws Exception {
        mockMvc.perform(get("/api/v1/preview"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(2)
    @WithMockUser
    void getPreview_returns200_withAllSections() throws Exception {
        mockMvc.perform(get("/api/v1/preview"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.experiences").isArray())
                .andExpect(jsonPath("$.data.projects").isArray())
                .andExpect(jsonPath("$.data.education").isArray())
                .andExpect(jsonPath("$.data.skills").isArray())
                .andExpect(jsonPath("$.data.certifications").isArray())
                .andExpect(jsonPath("$.data.socialLinks").isArray());
    }

    @Test
    @Order(3)
    @WithMockUser
    void getPreview_withLocaleEn_returns200() throws Exception {
        mockMvc.perform(get("/api/v1/preview").param("locale", "en"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").exists());
    }
}
