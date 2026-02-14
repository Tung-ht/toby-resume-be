package com.tobyresume.backend.content.hero;

import com.tobyresume.backend.common.dto.ApiResponse;
import com.tobyresume.backend.content.hero.dto.HeroRequest;
import com.tobyresume.backend.content.hero.dto.HeroResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Hero singleton section. GET returns draft or null; PUT creates/updates draft. JWT required (enforced by SecurityConfig).
 *
 * @see docs/ai/design/api-design.md §4.1
 */
@RestController
@RequestMapping("/api/v1")
public class HeroController {

    private final HeroService heroService;

    public HeroController(HeroService heroService) {
        this.heroService = heroService;
    }

    @GetMapping("/hero")
    public ResponseEntity<ApiResponse<HeroResponse>> getHero() {
        HeroResponse data = heroService.getDraft();
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @PutMapping("/hero")
    public ResponseEntity<ApiResponse<HeroResponse>> putHero(@Valid @RequestBody HeroRequest request) {
        HeroResponse data = heroService.upsertDraft(request);
        return ResponseEntity.ok(ApiResponse.success(data));
    }
}
