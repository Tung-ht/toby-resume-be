package com.tobyresume.backend.content.project;

import com.tobyresume.backend.common.dto.ApiResponse;
import com.tobyresume.backend.common.dto.ReorderRequest;
import com.tobyresume.backend.content.project.dto.ProjectItemRequest;
import com.tobyresume.backend.content.project.dto.ProjectItemResponse;
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
 * Projects list section. All endpoints operate on DRAFT. JWT required.
 *
 * @see docs/ai/design/api-design.md §4.3
 */
@RestController
@RequestMapping("/api/v1")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping("/projects")
    public ResponseEntity<ApiResponse<List<ProjectItemResponse>>> list() {
        return ResponseEntity.ok(ApiResponse.success(projectService.list()));
    }

    @GetMapping("/projects/{itemId}")
    public ResponseEntity<ApiResponse<ProjectItemResponse>> get(@PathVariable String itemId) {
        return ResponseEntity.ok(ApiResponse.success(projectService.get(itemId)));
    }

    @PostMapping("/projects")
    public ResponseEntity<ApiResponse<ProjectItemResponse>> add(@Valid @RequestBody ProjectItemRequest request) {
        return ResponseEntity.ok(ApiResponse.success(projectService.add(request)));
    }

    @PutMapping("/projects/{itemId}")
    public ResponseEntity<ApiResponse<ProjectItemResponse>> update(
            @PathVariable String itemId,
            @Valid @RequestBody ProjectItemRequest request) {
        return ResponseEntity.ok(ApiResponse.success(projectService.update(itemId, request)));
    }

    @DeleteMapping("/projects/{itemId}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String itemId) {
        projectService.delete(itemId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PutMapping("/projects/reorder")
    public ResponseEntity<ApiResponse<Void>> reorder(@Valid @RequestBody ReorderRequest request) {
        projectService.reorder(request.getOrderedIds());
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
