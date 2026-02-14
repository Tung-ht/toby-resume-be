package com.tobyresume.backend.content.experience;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tobyresume.backend.common.dto.ReorderRequest;
import com.tobyresume.backend.content.experience.dto.ExperienceItemRequest;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
class ExperienceControllerIntegrationTest {

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
        mockMvc.perform(get("/api/v1/experiences"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(2)
    @WithMockUser
    void list_returnsEmptyArray_whenNoDraft() throws Exception {
        mockMvc.perform(get("/api/v1/experiences"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    @Test
    @Order(3)
    @WithMockUser
    void add_returns200_andItemWithId() throws Exception {
        ExperienceItemRequest request = new ExperienceItemRequest();
        request.setCompany(Map.of("en", "TechCorp"));
        request.setRole(Map.of("en", "Engineer"));
        request.setStartDate("2023-01");

        String itemId = mockMvc.perform(post("/api/v1/experiences")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.itemId").isNotEmpty())
                .andExpect(jsonPath("$.data.company.en").value("TechCorp"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Extract itemId for later tests (optional: use jsonPath)
        org.hamcrest.MatcherAssert.assertThat(itemId, org.hamcrest.CoreMatchers.containsString("itemId"));
    }

    @Test
    @Order(4)
    @WithMockUser
    void list_returnsItemsAfterAdd() throws Exception {
        ExperienceItemRequest request = new ExperienceItemRequest();
        request.setCompany(Map.of("en", "Startup"));
        request.setRole(Map.of("en", "Dev"));
        request.setStartDate("2022-06");
        mockMvc.perform(post("/api/v1/experiences")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/experiences"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(org.hamcrest.Matchers.greaterThanOrEqualTo(1)));
    }

    @Test
    @Order(5)
    @WithMockUser
    void get_returns404_whenItemIdNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/experiences/nonexistent-id-12345"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("RESOURCE_NOT_FOUND"));
    }

    @Test
    @Order(6)
    @WithMockUser
    void add_thenGet_returnsSameItem() throws Exception {
        ExperienceItemRequest request = new ExperienceItemRequest();
        request.setCompany(Map.of("en", "GetTest Corp"));
        request.setRole(Map.of("en", "QA"));
        request.setStartDate("2021-01");
        String body = mockMvc.perform(post("/api/v1/experiences")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        String itemId = com.jayway.jsonpath.JsonPath.read(body, "$.data.itemId");

        mockMvc.perform(get("/api/v1/experiences/" + itemId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.itemId").value(itemId))
                .andExpect(jsonPath("$.data.company.en").value("GetTest Corp"));
    }

    @Test
    @Order(7)
    @WithMockUser
    void put_returns400_whenValidationFails() throws Exception {
        ExperienceItemRequest request = new ExperienceItemRequest();
        request.setRole(Map.of("en", "Role only"));
        request.setStartDate("2023-01");
        // missing company

        mockMvc.perform(put("/api/v1/experiences/some-id")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
    }

    @Test
    @Order(8)
    @WithMockUser
    void reorder_returns400_whenOrderedIdsMismatch() throws Exception {
        ReorderRequest request = new ReorderRequest();
        request.setOrderedIds(List.of("id-not-in-draft"));

        mockMvc.perform(put("/api/v1/experiences/reorder")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
    }
}
