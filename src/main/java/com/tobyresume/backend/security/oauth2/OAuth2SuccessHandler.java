package com.tobyresume.backend.security.oauth2;

import com.tobyresume.backend.config.AppSecurityProperties;
import com.tobyresume.backend.security.jwt.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.List;

/**
 * After OAuth2 callback: if user is in allowed-admins, generate JWT and redirect to Admin Panel with token.
 * Otherwise redirect with error=forbidden.
 *
 * @see docs/ai/design/api-design.md §3.1, phase1-mvp §7.1
 */
@Component
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final List<String> allowedAdmins;
    private final String redirectUri;

    public OAuth2SuccessHandler(JwtTokenProvider jwtTokenProvider, AppSecurityProperties appSecurity) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.allowedAdmins = appSecurity.getAllowedAdmins();
        this.redirectUri = appSecurity.getOauth2().getRedirectUri();
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException {
        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
        String registrationId = getRegistrationId(authentication);
        if (registrationId == null) {
            redirectWithError(response, "unknown_provider");
            return;
        }

        String sub;
        String name;
        if ("google".equals(registrationId)) {
            sub = getAttribute(oauth2User, "email", "sub");
            name = getAttribute(oauth2User, "name", "");
        } else if ("github".equals(registrationId)) {
            Object id = oauth2User.getAttribute("id");
            String email = oauth2User.getAttribute("email");
            sub = email != null && !email.isBlank() ? email : "github:" + (id != null ? id.toString() : "");
            name = getAttribute(oauth2User, "name", "login");
        } else {
            sub = oauth2User.getName();
            name = getAttribute(oauth2User, "name", "");
        }

        if (sub == null || sub.isBlank()) {
            redirectWithError(response, "missing_identity");
            return;
        }

        boolean allowed = isAllowed(oauth2User, registrationId, sub);
        if (!allowed) {
            redirectWithError(response, "forbidden");
            return;
        }

        String token = jwtTokenProvider.generateToken(sub, name, registrationId, "ADMIN");
        String targetUrl = UriComponentsBuilder.fromUriString(redirectUri)
                .queryParam("token", token)
                .build()
                .toUriString();
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    private String getRegistrationId(Authentication authentication) {
        if (authentication instanceof OAuth2AuthenticationToken token) {
            return token.getAuthorizedClientRegistrationId();
        }
        return null;
    }

    private String getAttribute(OAuth2User user, String primary, String fallbackKey) {
        String value = user.getAttribute(primary);
        if (value != null && !value.isBlank()) {
            return value;
        }
        return fallbackKey != null ? user.getAttribute(fallbackKey) : null;
    }

    private boolean isAllowed(OAuth2User oauth2User, String registrationId, String sub) {
        if (allowedAdmins.isEmpty()) {
            return false;
        }
        if (allowedAdmins.contains(sub)) {
            return true;
        }
        if ("github".equals(registrationId)) {
            Object id = oauth2User.getAttribute("id");
            if (id != null && allowedAdmins.contains(id.toString())) {
                return true;
            }
        }
        return false;
    }

    private void redirectWithError(HttpServletResponse response, String error) throws IOException {
        String targetUrl = UriComponentsBuilder.fromUriString(redirectUri)
                .queryParam("error", error)
                .build()
                .toUriString();
        response.sendRedirect(targetUrl);
    }
}
