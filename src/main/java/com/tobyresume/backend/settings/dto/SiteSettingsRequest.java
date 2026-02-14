package com.tobyresume.backend.settings.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

/**
 * Request body for PUT /api/v1/settings.
 *
 * @see docs/ai/design/api-design.md §6
 */
public class SiteSettingsRequest {

    /** Supported locale codes. Phase 1: exactly ["en", "vi"]. */
    @NotNull(message = "supportedLocales is required")
    @NotEmpty(message = "supportedLocales must not be empty")
    private String[] supportedLocales;

    /** Default locale; must be one of supportedLocales. */
    @NotNull(message = "defaultLocale is required")
    private String defaultLocale;

    /** Per-section PDF visibility. Keys: hero, experiences, projects, education, skills, certifications, socialLinks. */
    @NotNull(message = "pdfSectionVisibility is required")
    private Map<String, Boolean> pdfSectionVisibility;

    public String[] getSupportedLocales() {
        return supportedLocales;
    }

    public void setSupportedLocales(String[] supportedLocales) {
        this.supportedLocales = supportedLocales;
    }

    public String getDefaultLocale() {
        return defaultLocale;
    }

    public void setDefaultLocale(String defaultLocale) {
        this.defaultLocale = defaultLocale;
    }

    public Map<String, Boolean> getPdfSectionVisibility() {
        return pdfSectionVisibility;
    }

    public void setPdfSectionVisibility(Map<String, Boolean> pdfSectionVisibility) {
        this.pdfSectionVisibility = pdfSectionVisibility;
    }
}
