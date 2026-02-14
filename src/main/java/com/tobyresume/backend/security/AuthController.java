package com.tobyresume.backend.security;

import com.tobyresume.backend.common.dto.ApiResponse;
import com.tobyresume.backend.common.dto.ErrorBody;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Auth endpoints: current user (from JWT) and logout (client discards token).
 *
 * @see docs/ai/design/api-design.md §3.3
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<AuthMeResponse>> me(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthPrincipal principal)) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.error(new ErrorBody("UNAUTHORIZED", "Not authenticated", null)));
        }
        AuthMeResponse data = new AuthMeResponse(
                principal.email(),
                principal.name(),
                principal.role(),
                principal.provider());
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout() {
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    public record AuthMeResponse(String email, String name, String role, String provider) {
    }
}
