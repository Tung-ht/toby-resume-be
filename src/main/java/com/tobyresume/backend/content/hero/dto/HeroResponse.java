package com.tobyresume.backend.content.hero.dto;

import java.time.Instant;
import java.util.Map;

/**
 * Response body for GET/PUT /api/v1/hero. Includes updatedAt per API design.
 *
 * @see docs/ai/design/api-design.md §4.1
 */
public class HeroResponse {

    private Map<String, String> tagline;
    private Map<String, String> bio;
    private Map<String, String> fullName;
    private Map<String, String> title;
    private String profilePhotoMediaId;
    private Instant updatedAt;

    public Map<String, String> getTagline() {
        return tagline;
    }

    public void setTagline(Map<String, String> tagline) {
        this.tagline = tagline;
    }

    public Map<String, String> getBio() {
        return bio;
    }

    public void setBio(Map<String, String> bio) {
        this.bio = bio;
    }

    public Map<String, String> getFullName() {
        return fullName;
    }

    public void setFullName(Map<String, String> fullName) {
        this.fullName = fullName;
    }

    public Map<String, String> getTitle() {
        return title;
    }

    public void setTitle(Map<String, String> title) {
        this.title = title;
    }

    public String getProfilePhotoMediaId() {
        return profilePhotoMediaId;
    }

    public void setProfilePhotoMediaId(String profilePhotoMediaId) {
        this.profilePhotoMediaId = profilePhotoMediaId;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
