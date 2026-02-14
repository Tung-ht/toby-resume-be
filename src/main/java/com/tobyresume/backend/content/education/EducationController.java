package com.tobyresume.backend.content.education;

import com.tobyresume.backend.common.dto.ApiResponse;
import com.tobyresume.backend.common.dto.ReorderRequest;
import com.tobyresume.backend.content.education.dto.EducationItemRequest;
import com.tobyresume.backend.content.education.dto.EducationItemResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Education list section. All endpoints operate on DRAFT. JWT required.
 *
 * @see docs/ai/design/api-design.md §4.4
 */
@RestController
@RequestMapping("/api/v1")
public class EducationController {

    private final EducationService educationService;

    public EducationController(EducationService educationService) {
        this.educationService = educationService;
    }

    @GetMapping("/education")
    public ResponseEntity<ApiResponse<List<EducationItemResponse>>> list() {
        return ResponseEntity.ok(ApiResponse.success(educationService.list()));
    }

    @GetMapping("/education/{itemId}")
    public ResponseEntity<ApiResponse<EducationItemResponse>> get(@PathVariable String itemId) {
        return ResponseEntity.ok(ApiResponse.success(educationService.get(itemId)));
    }

    @PostMapping("/education")
    public ResponseEntity<ApiResponse<EducationItemResponse>> add(@Valid @RequestBody EducationItemRequest request) {
        return ResponseEntity.ok(ApiResponse.success(educationService.add(request)));
    }

    @PutMapping("/education/{itemId}")
    public ResponseEntity<ApiResponse<EducationItemResponse>> update(@PathVariable String itemId, @Valid @RequestBody EducationItemRequest request) {
        return ResponseEntity.ok(ApiResponse.success(educationService.update(itemId, request)));
    }

    @DeleteMapping("/education/{itemId}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String itemId) {
        educationService.delete(itemId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PutMapping("/education/reorder")
    public ResponseEntity<ApiResponse<Void>> reorder(@Valid @RequestBody ReorderRequest request) {
        educationService.reorder(request.getOrderedIds());
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
