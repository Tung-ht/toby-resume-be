package com.tobyresume.backend.settings.dto;

import java.time.Instant;
import java.util.Map;

/**
 * Response body for GET and PUT /api/v1/settings.
 *
 * @see docs/ai/design/api-design.md §6
 */
public class SiteSettingsResponse {

    private String id;
    private String[] supportedLocales;
    private String defaultLocale;
    private Map<String, Boolean> pdfSectionVisibility;
    private Instant createdAt;
    private Instant updatedAt;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

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

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
