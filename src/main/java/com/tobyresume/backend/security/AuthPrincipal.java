package com.tobyresume.backend.security;

/**
 * Principal set in SecurityContext after JWT validation. Holds user identity for /api/v1/**.
 */
public record AuthPrincipal(String email, String name, String role, String provider) {
}
