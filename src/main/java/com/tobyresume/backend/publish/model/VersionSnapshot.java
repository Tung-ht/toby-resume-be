package com.tobyresume.backend.publish.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;
import java.util.Map;

/**
 * Full snapshot of all content sections at publish time. One document per publish.
 *
 * @see docs/ai/design/database-design.md §5.9
 */
@Document(collection = "version_snapshots")
public class VersionSnapshot {

    @Id
    private String id;

    /** Nested map: hero, experiences, projects, education, skills, certifications, socialLinks. */
    private Map<String, Object> content;

    /** Optional human-readable label. */
    private String label;

    @Field("publishedAt")
    private Instant publishedAt;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Map<String, Object> getContent() {
        return content;
    }

    public void setContent(Map<String, Object> content) {
        this.content = content;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Instant getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(Instant publishedAt) {
        this.publishedAt = publishedAt;
    }
}
