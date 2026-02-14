package com.tobyresume.backend.publish.dto;

/**
 * Optional request body for POST /api/v1/publish.
 *
 * @see docs/ai/design/api-design.md §5.2
 */
public class PublishRequest {

    /** Optional human-readable label for the version. */
    private String label;

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}
