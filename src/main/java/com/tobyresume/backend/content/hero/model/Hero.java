package com.tobyresume.backend.content.hero.model;

import com.tobyresume.backend.common.model.BaseDocument;
import com.tobyresume.backend.common.model.ContentState;
import com.tobyresume.backend.common.validation.ValidLocaleKeys;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Map;

/**
 * Hero / About Me section. One document per content state (DRAFT, PUBLISHED).
 *
 * @see docs/ai/design/database-design.md §5.1
 */
@Document(collection = "hero")
public class Hero extends BaseDocument {

    @Indexed(unique = true)
    private ContentState contentState;

    @ValidLocaleKeys
    private Map<String, String> tagline;

    @ValidLocaleKeys
    private Map<String, String> bio;

    @ValidLocaleKeys
    private Map<String, String> fullName;

    @ValidLocaleKeys
    private Map<String, String> title;

    private String profilePhotoMediaId;

    public ContentState getContentState() {
        return contentState;
    }

    public void setContentState(ContentState contentState) {
        this.contentState = contentState;
    }

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
