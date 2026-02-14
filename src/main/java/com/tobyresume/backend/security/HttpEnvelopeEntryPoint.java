package com.tobyresume.backend.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tobyresume.backend.common.dto.ApiResponse;
import com.tobyresume.backend.common.dto.ErrorBody;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;

/**
 * Returns 401 with REST error envelope (UNAUTHORIZED) when an unauthenticated request hits a protected path.
 */
public class HttpEnvelopeEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ErrorBody error = new ErrorBody("UNAUTHORIZED", "Missing or invalid authentication", null);
        objectMapper.writeValue(response.getOutputStream(), ApiResponse.error(error));
    }
}
