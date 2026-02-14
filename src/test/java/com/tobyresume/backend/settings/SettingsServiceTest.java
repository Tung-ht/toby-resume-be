package com.tobyresume.backend.settings;

import com.tobyresume.backend.common.exception.ValidationException;
import com.tobyresume.backend.settings.dto.SiteSettingsRequest;
import com.tobyresume.backend.settings.dto.SiteSettingsResponse;
import com.tobyresume.backend.settings.model.SiteSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SettingsServiceTest {

    @Mock
    private SettingsRepository repository;

    @Mock
    private SettingsMapper mapper;

    @InjectMocks
    private SettingsService settingsService;

    private static Map<String, Boolean> validPdfVisibility() {
        Map<String, Boolean> m = new LinkedHashMap<>();
        m.put("hero", true);
        m.put("experiences", true);
        m.put("projects", true);
        m.put("education", true);
        m.put("skills", true);
        m.put("certifications", true);
        m.put("socialLinks", false);
        return m;
    }

    @Test
    void getOrCreate_returnsResponse_whenDocumentExists() {
        SiteSettings entity = new SiteSettings();
        entity.setDefaultLocale("en");
        SiteSettingsResponse response = new SiteSettingsResponse();
        response.setDefaultLocale("en");
        when(repository.findSingleton()).thenReturn(entity);
        when(mapper.toResponse(entity)).thenReturn(response);

        SiteSettingsResponse result = settingsService.getOrCreate();

        assertThat(result).isSameAs(response);
        verify(repository).findSingleton();
    }

    @Test
    void getOrCreate_createsAndReturnsDefaults_whenNoDocument() {
        when(repository.findSingleton()).thenReturn(null);
        ArgumentCaptor<SiteSettings> captor = ArgumentCaptor.forClass(SiteSettings.class);
        when(repository.save(captor.capture())).thenAnswer(inv -> inv.getArgument(0));
        SiteSettingsResponse response = new SiteSettingsResponse();
        response.setDefaultLocale("en");
        when(mapper.toResponse(any(SiteSettings.class))).thenReturn(response);

        SiteSettingsResponse result = settingsService.getOrCreate();

        assertThat(result).isNotNull();
        verify(repository).save(any(SiteSettings.class));
        SiteSettings saved = captor.getValue();
        assertThat(saved.getDefaultLocale()).isEqualTo("en");
        assertThat(saved.getSupportedLocales()).containsExactly("en", "vi");
    }

    @Test
    void update_throws_whenSupportedLocalesInvalid() {
        SiteSettingsRequest request = new SiteSettingsRequest();
        request.setSupportedLocales(new String[] { "en" });
        request.setDefaultLocale("en");
        request.setPdfSectionVisibility(validPdfVisibility());

        assertThatThrownBy(() -> settingsService.update(request))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("supportedLocales must be exactly");
    }

    @Test
    void update_throws_whenDefaultLocaleNotInSupported() {
        SiteSettingsRequest request = new SiteSettingsRequest();
        request.setSupportedLocales(new String[] { "en", "vi" });
        request.setDefaultLocale("fr");
        request.setPdfSectionVisibility(validPdfVisibility());

        assertThatThrownBy(() -> settingsService.update(request))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("defaultLocale must be one of");
    }

    @Test
    void update_savesAndReturns_whenValid() {
        SiteSettingsRequest request = new SiteSettingsRequest();
        request.setSupportedLocales(new String[] { "en", "vi" });
        request.setDefaultLocale("vi");
        request.setPdfSectionVisibility(validPdfVisibility());

        SiteSettings entity = new SiteSettings();
        when(repository.findSingleton()).thenReturn(entity);
        when(repository.save(entity)).thenReturn(entity);
        SiteSettingsResponse response = new SiteSettingsResponse();
        when(mapper.toResponse(entity)).thenReturn(response);

        SiteSettingsResponse result = settingsService.update(request);

        assertThat(result).isSameAs(response);
        verify(repository).save(entity);
        assertThat(entity.getDefaultLocale()).isEqualTo("vi");
    }
}
