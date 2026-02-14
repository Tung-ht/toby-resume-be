package com.tobyresume.backend.publish;

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

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for publish pipeline: POST /api/v1/publish and GET /api/v1/publish/status.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@TestPropertySource(properties = {
        "app.cors.allowed-origins=http://localhost:3000",
        "app.security.jwt.secret=test-jwt-secret-at-least-32-characters-long",
        "app.security.jwt.expiration-ms=3600000",
        "app.security.oauth2.redirect-uri=http://localhost:3000/auth/callback"
})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PublishControllerIntegrationTest {

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
    void getStatus_unauthorized_withoutAuth() throws Exception {
        mockMvc.perform(get("/api/v1/publish/status"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(2)
    @WithMockUser
    void getStatus_returnsZero_whenNeverPublished() throws Exception {
        mockMvc.perform(get("/api/v1/publish/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.lastPublishedAt").isEmpty())
                .andExpect(jsonPath("$.data.versionCount").value(0));
    }

    @Test
    @Order(3)
    void postPublish_unauthorized_withoutAuth() throws Exception {
        mockMvc.perform(post("/api/v1/publish")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(4)
    @WithMockUser
    void postPublish_returns200_andCreatesSnapshot() throws Exception {
        mockMvc.perform(post("/api/v1/publish")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.versionId").isNotEmpty())
                .andExpect(jsonPath("$.data.publishedAt").isNotEmpty())
                .andExpect(jsonPath("$.data.sectionsPublished").isArray())
                .andExpect(jsonPath("$.data.sectionsPublished.length()").value(7));
    }

    @Test
    @Order(5)
    @WithMockUser
    void getStatus_returnsLastPublish_afterPublish() throws Exception {
        mockMvc.perform(post("/api/v1/publish")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"label\":\"v1.0\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/publish/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.lastPublishedAt").isNotEmpty())
                .andExpect(jsonPath("$.data.versionCount").value(greaterThanOrEqualTo(1)));
    }
}
