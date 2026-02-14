package com.tobyresume.backend.settings;

import com.tobyresume.backend.common.exception.ValidationException;
import com.tobyresume.backend.settings.dto.SiteSettingsRequest;
import com.tobyresume.backend.settings.dto.SiteSettingsResponse;
import com.tobyresume.backend.settings.model.SiteSettings;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Site settings: get or bootstrap default, update. Single document per app.
 *
 * @see docs/ai/design/database-design.md §5.8, §8.4
 * @see docs/ai/design/api-design.md §6
 */
@Service
public class SettingsService {

    /** Valid keys for pdfSectionVisibility. Phase 1 fixed set. */
    private static final List<String> PDF_SECTION_KEYS = List.of(
            "hero", "experiences", "projects", "education", "skills", "certifications", "socialLinks"
    );

    private static final String[] DEFAULT_SUPPORTED_LOCALES = new String[] { "en", "vi" };
    private static final String DEFAULT_LOCALE = "en";

    private final SettingsRepository repository;
    private final SettingsMapper mapper;

    public SettingsService(SettingsRepository repository, SettingsMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    /**
     * Returns the current site settings. If no document exists, creates one with defaults and returns it.
     */
    public SiteSettingsResponse getOrCreate() {
        SiteSettings entity = repository.findSingleton();
        if (entity == null) {
            entity = createDefaultSettings();
            repository.save(entity);
        }
        return mapper.toResponse(entity);
    }

    /**
     * Updates site settings from the request. Validates defaultLocale and pdfSectionVisibility keys.
     * Creates default document first if none exists.
     */
    public SiteSettingsResponse update(SiteSettingsRequest request) {
        validateRequest(request);

        SiteSettings entity = repository.findSingleton();
        if (entity == null) {
            entity = createDefaultSettings();
        }

        entity.setSupportedLocales(request.getSupportedLocales());
        entity.setDefaultLocale(request.getDefaultLocale());
        entity.setPdfSectionVisibility(request.getPdfSectionVisibility());

        repository.save(entity);
        return mapper.toResponse(entity);
    }

    private SiteSettings createDefaultSettings() {
        SiteSettings settings = new SiteSettings();
        settings.setSupportedLocales(DEFAULT_SUPPORTED_LOCALES);
        settings.setDefaultLocale(DEFAULT_LOCALE);
        settings.setPdfSectionVisibility(defaultPdfSectionVisibility());
        return settings;
    }

    private static Map<String, Boolean> defaultPdfSectionVisibility() {
        Map<String, Boolean> map = new LinkedHashMap<>();
        for (String key : PDF_SECTION_KEYS) {
            map.put(key, !"socialLinks".equals(key));
        }
        return map;
    }

    private void validateRequest(SiteSettingsRequest request) {
        String defaultLocale = request.getDefaultLocale();
        String[] supported = request.getSupportedLocales();
        if (supported == null) {
            return; // Jakarta validation already requires non-null
        }
        // Phase 1: only ["en", "vi"] allowed (api-design §6).
        Set<String> supportedSet = Arrays.stream(supported).collect(Collectors.toSet());
        if (!supportedSet.equals(Set.of("en", "vi"))) {
            throw new ValidationException("supportedLocales must be exactly [\"en\", \"vi\"] for Phase 1");
        }
        if (!supportedSet.contains(defaultLocale)) {
            throw new ValidationException("defaultLocale must be one of supportedLocales: " + Arrays.toString(supported));
        }

        Map<String, Boolean> pdf = request.getPdfSectionVisibility();
        if (pdf == null) {
            return;
        }
        Set<String> pdfKeys = pdf.keySet();
        if (!pdfKeys.equals(Set.copyOf(PDF_SECTION_KEYS))) {
            throw new ValidationException(
                    "pdfSectionVisibility must have exactly these keys: " + PDF_SECTION_KEYS + "; got: " + pdfKeys);
        }
    }
}
