package com.tobyresume.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * CORS configuration from app.cors.*. Allows Admin Panel and Landing Page origins.
 *
 * @see docs/ai/design/api-design.md §1.2, phase1-mvp §10
 */
@Configuration
public class CorsConfig {

    @Value("${app.cors.allowed-origins:http://localhost:3000,http://localhost:3001}")
    private String allowedOriginsConfig;

    @Value("${app.cors.allowed-methods:GET,POST,PUT,DELETE,OPTIONS}")
    private String allowedMethods;

    @Value("${app.cors.allowed-headers:Content-Type,Authorization}")
    private String allowedHeaders;

    @Value("${app.cors.allow-credentials:true}")
    private boolean allowCredentials;

    @Value("${app.cors.max-age:3600}")
    private long maxAge;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(resolveAllowedOrigins());
        config.setAllowedMethods(splitComma(allowedMethods));
        config.setAllowedHeaders(splitComma(allowedHeaders));
        config.setAllowCredentials(allowCredentials);
        config.setMaxAge(maxAge);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    private List<String> resolveAllowedOrigins() {
        if (allowedOriginsConfig == null || allowedOriginsConfig.isBlank()) {
            return List.of();
        }
        if (allowedOriginsConfig.startsWith("[")) {
            return parseYamlListStyle(allowedOriginsConfig);
        }
        return splitComma(allowedOriginsConfig);
    }

    private static List<String> parseYamlListStyle(String value) {
        String trimmed = value.replace("[", "").replace("]", "").trim();
        return splitComma(trimmed);
    }

    private static List<String> splitComma(String value) {
        if (value == null || value.isBlank()) {
            return List.of();
        }
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }
}
