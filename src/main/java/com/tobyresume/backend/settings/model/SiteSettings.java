package com.tobyresume.backend.settings.model;

import com.tobyresume.backend.common.model.BaseDocument;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Map;

/**
 * Global site configuration. Exactly one document in the collection.
 * Bootstrapped with defaults on first access if absent.
 *
 * @see docs/ai/design/database-design.md §5.8
 */
@Document(collection = "site_settings")
public class SiteSettings extends BaseDocument {

    /** Supported locale codes. Phase 1: ["en", "vi"]. */
    private String[] supportedLocales;

    /** Default locale for content when client does not specify one. */
    private String defaultLocale;

    /** Per-section visibility for PDF export. Keys: hero, experiences, projects, education, skills, certifications, socialLinks. */
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
