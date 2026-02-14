package com.tobyresume.backend.settings;

import com.tobyresume.backend.settings.model.SiteSettings;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/**
 * Repository for site_settings. Collection holds exactly one document.
 * Use findAll() and take the first result, or insert when empty.
 *
 * @see docs/ai/design/database-design.md §5.8, §7
 */
public interface SettingsRepository extends MongoRepository<SiteSettings, String> {

    /**
     * Returns the single settings document if present. Collection has at most one document.
     */
    default SiteSettings findSingleton() {
        List<SiteSettings> all = findAll();
        return all.isEmpty() ? null : all.get(0);
    }
}
