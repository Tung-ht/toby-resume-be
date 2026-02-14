package com.tobyresume.backend.content.skill;

import com.tobyresume.backend.common.dto.ApiResponse;
import com.tobyresume.backend.common.dto.ReorderRequest;
import com.tobyresume.backend.content.skill.dto.SkillCategoryRequest;
import com.tobyresume.backend.content.skill.dto.SkillCategoryResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Skills section (categories with items). All endpoints operate on DRAFT. JWT required.
 *
 * @see docs/ai/design/api-design.md §4.5
 */
@RestController
@RequestMapping("/api/v1")
public class SkillController {

    private final SkillService skillService;

    public SkillController(SkillService skillService) {
        this.skillService = skillService;
    }

    @GetMapping("/skills")
    public ResponseEntity<ApiResponse<List<SkillCategoryResponse>>> list() {
        return ResponseEntity.ok(ApiResponse.success(skillService.list()));
    }

    @GetMapping("/skills/{categoryId}")
    public ResponseEntity<ApiResponse<SkillCategoryResponse>> get(@PathVariable String categoryId) {
        return ResponseEntity.ok(ApiResponse.success(skillService.get(categoryId)));
    }

    @PostMapping("/skills")
    public ResponseEntity<ApiResponse<SkillCategoryResponse>> add(@Valid @RequestBody SkillCategoryRequest request) {
        return ResponseEntity.ok(ApiResponse.success(skillService.add(request)));
    }

    @PutMapping("/skills/{categoryId}")
    public ResponseEntity<ApiResponse<SkillCategoryResponse>> update(@PathVariable String categoryId, @Valid @RequestBody SkillCategoryRequest request) {
        return ResponseEntity.ok(ApiResponse.success(skillService.update(categoryId, request)));
    }

    @DeleteMapping("/skills/{categoryId}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String categoryId) {
        skillService.delete(categoryId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PutMapping("/skills/reorder")
    public ResponseEntity<ApiResponse<Void>> reorder(@Valid @RequestBody ReorderRequest request) {
        skillService.reorder(request.getOrderedIds());
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
