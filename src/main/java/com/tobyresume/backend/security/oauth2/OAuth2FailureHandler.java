package com.tobyresume.backend.security.oauth2;

import com.tobyresume.backend.config.AppSecurityProperties;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

/**
 * On OAuth2 login failure (e.g. user denies consent), redirect to Admin Panel with error param.
 *
 * @see docs/ai/design/phase1-mvp §7.1
 */
@Component
public class OAuth2FailureHandler extends SimpleUrlAuthenticationFailureHandler {

    private final String redirectUri;

    public OAuth2FailureHandler(AppSecurityProperties appSecurity) {
        this.redirectUri = appSecurity.getOauth2().getRedirectUri();
    }

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception) throws IOException {
        String targetUrl = UriComponentsBuilder.fromUriString(redirectUri)
                .queryParam("error", "access_denied")
                .build()
                .toUriString();
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
