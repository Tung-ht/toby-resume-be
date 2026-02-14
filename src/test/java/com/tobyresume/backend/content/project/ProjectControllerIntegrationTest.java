package com.tobyresume.backend.content.project;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tobyresume.backend.common.dto.ReorderRequest;
import com.tobyresume.backend.content.project.dto.ProjectItemRequest;
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

import java.util.List;
import java.util.Map;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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
class ProjectControllerIntegrationTest {

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
    void list_unauthorized_withoutAuth() throws Exception {
        mockMvc.perform(get("/api/v1/projects")).andExpect(status().isUnauthorized());
    }

    @Test
    @Order(2)
    @WithMockUser
    void list_returnsEmptyArray_whenNoDraft() throws Exception {
        mockMvc.perform(get("/api/v1/projects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    @Test
    @Order(3)
    @WithMockUser
    void add_returns200_andItemWithId() throws Exception {
        ProjectItemRequest request = new ProjectItemRequest();
        request.setTitle(Map.of("en", "Portfolio CMS"));
        request.setVisible(true);

        mockMvc.perform(post("/api/v1/projects")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.itemId").isNotEmpty())
                .andExpect(jsonPath("$.data.title.en").value("Portfolio CMS"));
    }

    @Test
    @Order(4)
    @WithMockUser
    void get_returns404_whenItemIdNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/projects/nonexistent-id-12345"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("RESOURCE_NOT_FOUND"));
    }

    @Test
    @Order(5)
    @WithMockUser
    void put_returns400_whenValidationFails() throws Exception {
        ProjectItemRequest request = new ProjectItemRequest();
        request.setVisible(true);
        // missing title

        mockMvc.perform(put("/api/v1/projects/some-id")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
    }

    @Test
    @Order(6)
    @WithMockUser
    void reorder_returns400_whenOrderedIdsMismatch() throws Exception {
        ReorderRequest request = new ReorderRequest();
        request.setOrderedIds(List.of("id-not-in-draft"));
        mockMvc.perform(put("/api/v1/projects/reorder")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
    }
}
