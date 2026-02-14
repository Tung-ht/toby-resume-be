package com.tobyresume.backend.graphql;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@TestPropertySource(properties = {
        "app.security.jwt.secret=test-jwt-secret-at-least-32-characters-long",
        "app.security.oauth2.redirect-uri=http://localhost:3000/auth/callback"
})
class GraphQLIntegrationTest {

    @Container
    static MongoDBContainer mongo = new MongoDBContainer("mongo:7");

    @DynamicPropertySource
    static void mongoProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongo::getReplicaSetUrl);
    }

    @Autowired
    private MockMvc mockMvc;

    @Test
    void graphql_siteSettingsQuery_returns200AndData() throws Exception {
        mockMvc.perform(post("/graphql")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"query\":\"query { siteSettings { defaultLocale supportedLocales } }\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.siteSettings.defaultLocale").exists())
                .andExpect(jsonPath("$.data.siteSettings.supportedLocales").isArray());
    }

    @Test
    void graphql_heroQuery_withLocale_returns200() throws Exception {
        mockMvc.perform(post("/graphql")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"query\":\"query { hero(locale: EN) { tagline fullName } }\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").exists());
    }
}
