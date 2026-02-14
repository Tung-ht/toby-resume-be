package com.tobyresume.backend.content.experience;

import com.tobyresume.backend.common.dto.ApiResponse;
import com.tobyresume.backend.common.dto.ReorderRequest;
import com.tobyresume.backend.content.experience.dto.ExperienceItemRequest;
import com.tobyresume.backend.content.experience.dto.ExperienceItemResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Work experience list section. All endpoints operate on DRAFT. JWT required (SecurityConfig).
 *
 * @see docs/ai/design/api-design.md §4.2
 */
@RestController
@RequestMapping("/api/v1")
public class ExperienceController {

    private final ExperienceService experienceService;

    public ExperienceController(ExperienceService experienceService) {
        this.experienceService = experienceService;
    }

    @GetMapping("/experiences")
    public ResponseEntity<ApiResponse<List<ExperienceItemResponse>>> list() {
        List<ExperienceItemResponse> data = experienceService.list();
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @GetMapping("/experiences/{itemId}")
    public ResponseEntity<ApiResponse<ExperienceItemResponse>> get(@PathVariable String itemId) {
        ExperienceItemResponse data = experienceService.get(itemId);
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @PostMapping("/experiences")
    public ResponseEntity<ApiResponse<ExperienceItemResponse>> add(@Valid @RequestBody ExperienceItemRequest request) {
        ExperienceItemResponse data = experienceService.add(request);
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @PutMapping("/experiences/{itemId}")
    public ResponseEntity<ApiResponse<ExperienceItemResponse>> update(
            @PathVariable String itemId,
            @Valid @RequestBody ExperienceItemRequest request) {
        ExperienceItemResponse data = experienceService.update(itemId, request);
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @DeleteMapping("/experiences/{itemId}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String itemId) {
        experienceService.delete(itemId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PutMapping("/experiences/reorder")
    public ResponseEntity<ApiResponse<Void>> reorder(@Valid @RequestBody ReorderRequest request) {
        experienceService.reorder(request.getOrderedIds());
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
