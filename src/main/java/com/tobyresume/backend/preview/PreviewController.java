package com.tobyresume.backend.preview;

import com.tobyresume.backend.common.dto.ApiResponse;
import com.tobyresume.backend.preview.dto.PreviewResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Preview endpoint: all DRAFT sections in one payload for admin preview.
 * JWT required (enforced by SecurityConfig for /api/v1/**).
 *
 * @see docs/ai/design/api-design.md §5.1
 */
@RestController
@RequestMapping("/api/v1")
public class PreviewController {

    private final PreviewService previewService;

    public PreviewController(PreviewService previewService) {
        this.previewService = previewService;
    }

    /**
     * GET /api/v1/preview — full draft payload.
     * GET /api/v1/preview?locale=en — same, with content filtered to single locale (en or vi).
     */
    @GetMapping("/preview")
    public ResponseEntity<ApiResponse<PreviewResponse>> getPreview(
            @RequestParam(required = false) String locale) {
        PreviewResponse data = previewService.getPreview(locale);
        return ResponseEntity.ok(ApiResponse.success(data));
    }
}
