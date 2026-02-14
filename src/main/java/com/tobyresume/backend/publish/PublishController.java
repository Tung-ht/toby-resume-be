package com.tobyresume.backend.publish;

import com.tobyresume.backend.common.dto.ApiResponse;
import com.tobyresume.backend.publish.dto.PublishRequest;
import com.tobyresume.backend.publish.dto.PublishResponse;
import com.tobyresume.backend.publish.dto.PublishStatusResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Publish pipeline: POST to copy DRAFT → PUBLISHED and create snapshot; GET status for last publish info.
 * JWT required (SecurityConfig /api/v1/**).
 *
 * @see docs/ai/design/api-design.md §5.2
 */
@RestController
@RequestMapping("/api/v1/publish")
public class PublishController {

    private final PublishService publishService;

    public PublishController(PublishService publishService) {
        this.publishService = publishService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PublishResponse>> publish(@RequestBody(required = false) PublishRequest request) {
        String label = request != null ? request.getLabel() : null;
        PublishService.PublishResult result = publishService.publish(label);

        PublishResponse data = new PublishResponse(
                result.versionId(),
                result.publishedAt(),
                result.sectionsPublished()
        );
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @GetMapping("/status")
    public ResponseEntity<ApiResponse<PublishStatusResponse>> status() {
        PublishService.PublishStatus status = publishService.getStatus();
        PublishStatusResponse data = new PublishStatusResponse(
                status.lastPublishedAt(),
                status.versionCount()
        );
        return ResponseEntity.ok(ApiResponse.success(data));
    }
}
