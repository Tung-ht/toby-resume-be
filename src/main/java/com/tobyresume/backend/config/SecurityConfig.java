package com.tobyresume.backend.config;

import com.tobyresume.backend.security.HttpEnvelopeAccessDeniedHandler;
import com.tobyresume.backend.security.HttpEnvelopeEntryPoint;
import com.tobyresume.backend.security.jwt.JwtAuthFilter;
import com.tobyresume.backend.security.oauth2.OAuth2FailureHandler;
import com.tobyresume.backend.security.oauth2.OAuth2SuccessHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Phase C: OAuth2 login (Google/GitHub), JWT for /api/v1/**, stateless sessions.
 * Public: /graphql, /actuator/**, /oauth2/**, /login/oauth2/**.
 *
 * @see docs/ai/design/phase1-mvp.md §7.3
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final OAuth2FailureHandler oAuth2FailureHandler;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter,
                          OAuth2SuccessHandler oAuth2SuccessHandler,
                          OAuth2FailureHandler oAuth2FailureHandler) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.oAuth2SuccessHandler = oAuth2SuccessHandler;
        this.oAuth2FailureHandler = oAuth2FailureHandler;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/graphql", "/graphql/**").permitAll()
                        .requestMatchers("/actuator/**").permitAll()
                        .requestMatchers("/oauth2/**", "/login/oauth2/**").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .requestMatchers("/api/v1/**").authenticated()
                        .anyRequest().denyAll())
                .oauth2Login(oauth -> oauth
                        .successHandler(oAuth2SuccessHandler)
                        .failureHandler(oAuth2FailureHandler))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(new HttpEnvelopeEntryPoint())
                        .accessDeniedHandler(new HttpEnvelopeAccessDeniedHandler()))
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
