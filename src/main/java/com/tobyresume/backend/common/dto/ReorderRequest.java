package com.tobyresume.backend.common.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * Request body for list-section reorder endpoints (e.g. PUT /api/v1/experiences/reorder).
 * Order of IDs in the list becomes the new order (0-based index).
 *
 * @see docs/ai/design/api-design.md §4.2, §4.3, etc.
 */
public class ReorderRequest {

    @NotNull(message = "orderedIds must not be null")
    private List<String> orderedIds;

    public List<String> getOrderedIds() {
        return orderedIds;
    }

    public void setOrderedIds(List<String> orderedIds) {
        this.orderedIds = orderedIds;
    }
}
