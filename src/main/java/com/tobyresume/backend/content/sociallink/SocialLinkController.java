package com.tobyresume.backend.content.sociallink;

import com.tobyresume.backend.common.dto.ApiResponse;
import com.tobyresume.backend.common.dto.ReorderRequest;
import com.tobyresume.backend.content.sociallink.dto.SocialLinkItemRequest;
import com.tobyresume.backend.content.sociallink.dto.SocialLinkItemResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Social links list section. All endpoints operate on DRAFT. JWT required.
 * API path: /api/v1/social-links (kebab-case per api-design §4.7).
 *
 * @see docs/ai/design/api-design.md §4.7
 */
@RestController
@RequestMapping("/api/v1")
public class SocialLinkController {

    private final SocialLinkService socialLinkService;

    public SocialLinkController(SocialLinkService socialLinkService) {
        this.socialLinkService = socialLinkService;
    }

    @GetMapping("/social-links")
    public ResponseEntity<ApiResponse<List<SocialLinkItemResponse>>> list() {
        return ResponseEntity.ok(ApiResponse.success(socialLinkService.list()));
    }

    @GetMapping("/social-links/{itemId}")
    public ResponseEntity<ApiResponse<SocialLinkItemResponse>> get(@PathVariable String itemId) {
        return ResponseEntity.ok(ApiResponse.success(socialLinkService.get(itemId)));
    }

    @PostMapping("/social-links")
    public ResponseEntity<ApiResponse<SocialLinkItemResponse>> add(@Valid @RequestBody SocialLinkItemRequest request) {
        return ResponseEntity.ok(ApiResponse.success(socialLinkService.add(request)));
    }

    @PutMapping("/social-links/{itemId}")
    public ResponseEntity<ApiResponse<SocialLinkItemResponse>> update(@PathVariable String itemId, @Valid @RequestBody SocialLinkItemRequest request) {
        return ResponseEntity.ok(ApiResponse.success(socialLinkService.update(itemId, request)));
    }

    @DeleteMapping("/social-links/{itemId}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String itemId) {
        socialLinkService.delete(itemId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PutMapping("/social-links/reorder")
    public ResponseEntity<ApiResponse<Void>> reorder(@Valid @RequestBody ReorderRequest request) {
        socialLinkService.reorder(request.getOrderedIds());
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
