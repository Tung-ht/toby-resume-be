package com.tobyresume.backend.publish.dto;

import java.time.Instant;
import java.util.List;

/**
 * Response data for POST /api/v1/publish.
 *
 * @see docs/ai/design/api-design.md §5.2
 */
public class PublishResponse {

    private String versionId;
    private Instant publishedAt;
    private List<String> sectionsPublished;

    public PublishResponse() {
    }

    public PublishResponse(String versionId, Instant publishedAt, List<String> sectionsPublished) {
        this.versionId = versionId;
        this.publishedAt = publishedAt;
        this.sectionsPublished = sectionsPublished;
    }

    public String getVersionId() {
        return versionId;
    }

    public void setVersionId(String versionId) {
        this.versionId = versionId;
    }

    public Instant getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(Instant publishedAt) {
        this.publishedAt = publishedAt;
    }

    public List<String> getSectionsPublished() {
        return sectionsPublished;
    }

    public void setSectionsPublished(List<String> sectionsPublished) {
        this.sectionsPublished = sectionsPublished;
    }
}
