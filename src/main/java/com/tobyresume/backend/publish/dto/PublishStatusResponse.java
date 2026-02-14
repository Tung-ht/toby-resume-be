package com.tobyresume.backend.publish.dto;

import java.time.Instant;

/**
 * Response data for GET /api/v1/publish/status.
 *
 * @see docs/ai/design/api-design.md §5.2
 */
public class PublishStatusResponse {

    /** Last publish time, or null if never published. */
    private Instant lastPublishedAt;
    private long versionCount;

    public PublishStatusResponse() {
    }

    public PublishStatusResponse(Instant lastPublishedAt, long versionCount) {
        this.lastPublishedAt = lastPublishedAt;
        this.versionCount = versionCount;
    }

    public Instant getLastPublishedAt() {
        return lastPublishedAt;
    }

    public void setLastPublishedAt(Instant lastPublishedAt) {
        this.lastPublishedAt = lastPublishedAt;
    }

    public long getVersionCount() {
        return versionCount;
    }

    public void setVersionCount(long versionCount) {
        this.versionCount = versionCount;
    }
}
