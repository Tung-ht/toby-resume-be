package com.tobyresume.backend.content.hero.dto;

import com.tobyresume.backend.common.validation.MapValueMaxLength;
import com.tobyresume.backend.common.validation.ValidLocaleKeys;
import jakarta.validation.constraints.Size;

import java.util.Map;

/**
 * Request body for PUT /api/v1/hero. All fields optional; locale keys only "en" and "vi".
 *
 * @see docs/ai/design/api-design.md §4.1, database-design §9
 */
public class HeroRequest {

    @ValidLocaleKeys
    @MapValueMaxLength(500)
    private Map<String, String> tagline;

    @ValidLocaleKeys
    @MapValueMaxLength(2000)
    private Map<String, String> bio;

    @ValidLocaleKeys
    @MapValueMaxLength(200)
    private Map<String, String> fullName;

    @ValidLocaleKeys
    @MapValueMaxLength(200)
    private Map<String, String> title;

    @Size(max = 500)
    private String profilePhotoMediaId;

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
}
