package com.tobyresume.backend.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tobyresume.backend.common.dto.ApiResponse;
import com.tobyresume.backend.common.dto.ErrorBody;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;

/**
 * Returns 403 with REST error envelope (FORBIDDEN) when an authenticated user lacks permission.
 */
public class HttpEnvelopeAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ErrorBody error = new ErrorBody("FORBIDDEN", "Access denied", null);
        objectMapper.writeValue(response.getOutputStream(), ApiResponse.error(error));
    }
}
