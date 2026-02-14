package com.tobyresume.backend.content.certification;

import com.tobyresume.backend.common.dto.ApiResponse;
import com.tobyresume.backend.common.dto.ReorderRequest;
import com.tobyresume.backend.content.certification.dto.CertificationItemRequest;
import com.tobyresume.backend.content.certification.dto.CertificationItemResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Certifications list section. All endpoints operate on DRAFT. JWT required.
 *
 * @see docs/ai/design/api-design.md §4.6
 */
@RestController
@RequestMapping("/api/v1")
public class CertificationController {

    private final CertificationService certificationService;

    public CertificationController(CertificationService certificationService) {
        this.certificationService = certificationService;
    }

    @GetMapping("/certifications")
    public ResponseEntity<ApiResponse<List<CertificationItemResponse>>> list() {
        return ResponseEntity.ok(ApiResponse.success(certificationService.list()));
    }

    @GetMapping("/certifications/{itemId}")
    public ResponseEntity<ApiResponse<CertificationItemResponse>> get(@PathVariable String itemId) {
        return ResponseEntity.ok(ApiResponse.success(certificationService.get(itemId)));
    }

    @PostMapping("/certifications")
    public ResponseEntity<ApiResponse<CertificationItemResponse>> add(@Valid @RequestBody CertificationItemRequest request) {
        return ResponseEntity.ok(ApiResponse.success(certificationService.add(request)));
    }

    @PutMapping("/certifications/{itemId}")
    public ResponseEntity<ApiResponse<CertificationItemResponse>> update(@PathVariable String itemId, @Valid @RequestBody CertificationItemRequest request) {
        return ResponseEntity.ok(ApiResponse.success(certificationService.update(itemId, request)));
    }

    @DeleteMapping("/certifications/{itemId}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String itemId) {
        certificationService.delete(itemId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PutMapping("/certifications/reorder")
    public ResponseEntity<ApiResponse<Void>> reorder(@Valid @RequestBody ReorderRequest request) {
        certificationService.reorder(request.getOrderedIds());
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
