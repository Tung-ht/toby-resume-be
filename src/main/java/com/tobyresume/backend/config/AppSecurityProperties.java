package com.tobyresume.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Binds app.security.* for OAuth2 and JWT. Used by OAuth2SuccessHandler and JwtTokenProvider.
 */
@Component
@ConfigurationProperties(prefix = "app.security")
public class AppSecurityProperties {

    private List<String> allowedAdmins = List.of();
    private Jwt jwt = new Jwt();
    private Oauth2 oauth2 = new Oauth2();

    public List<String> getAllowedAdmins() {
        return allowedAdmins;
    }

    public void setAllowedAdmins(List<String> allowedAdmins) {
        this.allowedAdmins = allowedAdmins != null ? allowedAdmins : List.of();
    }

    public Jwt getJwt() {
        return jwt;
    }

    public void setJwt(Jwt jwt) {
        this.jwt = jwt != null ? jwt : new Jwt();
    }

    public Oauth2 getOauth2() {
        return oauth2;
    }

    public void setOauth2(Oauth2 oauth2) {
        this.oauth2 = oauth2 != null ? oauth2 : new Oauth2();
    }

    public static class Jwt {
        private String secret = "dev-jwt-secret-change-in-production";
        private long expirationMs = 86400000L;

        public String getSecret() {
            return secret;
        }

        public void setSecret(String secret) {
            this.secret = secret;
        }

        public long getExpirationMs() {
            return expirationMs;
        }

        public void setExpirationMs(long expirationMs) {
            this.expirationMs = expirationMs;
        }
    }

    public static class Oauth2 {
        private String redirectUri = "http://localhost:3000/auth/callback";

        public String getRedirectUri() {
            return redirectUri;
        }

        public void setRedirectUri(String redirectUri) {
            this.redirectUri = redirectUri;
        }
    }
}
