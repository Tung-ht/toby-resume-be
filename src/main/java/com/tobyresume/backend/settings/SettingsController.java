package com.tobyresume.backend.settings;

import com.tobyresume.backend.common.dto.ApiResponse;
import com.tobyresume.backend.settings.dto.SiteSettingsRequest;
import com.tobyresume.backend.settings.dto.SiteSettingsResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST API for site settings. GET returns current (or bootstrapped default); PUT updates.
 * JWT required (enforced by SecurityConfig for /api/v1/**).
 *
 * @see docs/ai/design/api-design.md §6
 */
@RestController
@RequestMapping("/api/v1/settings")
public class SettingsController {

    private final SettingsService settingsService;

    public SettingsController(SettingsService settingsService) {
        this.settingsService = settingsService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<SiteSettingsResponse>> get() {
        SiteSettingsResponse data = settingsService.getOrCreate();
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @PutMapping
    public ResponseEntity<ApiResponse<SiteSettingsResponse>> put(@Valid @RequestBody SiteSettingsRequest request) {
        SiteSettingsResponse data = settingsService.update(request);
        return ResponseEntity.ok(ApiResponse.success(data));
    }
}
