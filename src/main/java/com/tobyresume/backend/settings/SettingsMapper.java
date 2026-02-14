package com.tobyresume.backend.settings;

import com.tobyresume.backend.settings.dto.SiteSettingsResponse;
import com.tobyresume.backend.settings.model.SiteSettings;
import org.mapstruct.Mapper;

/**
 * MapStruct mapper: SiteSettings entity → SiteSettingsResponse.
 * Update from request is done explicitly in SettingsService for clarity.
 */
@Mapper(componentModel = "spring")
public interface SettingsMapper {

    SiteSettingsResponse toResponse(SiteSettings entity);
}
