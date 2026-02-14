package com.tobyresume.backend.content.project.dto;

import com.tobyresume.backend.common.validation.MapValueMaxLength;
import com.tobyresume.backend.common.validation.ValidLocaleKeys;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.Map;

/**
 * Request body for POST/PUT project item. itemId assigned by server on POST.
 *
 * @see docs/ai/design/api-design.md §4.3, database-design §9
 */
public class ProjectItemRequest {

    @NotNull(message = "title is required")
    @Size(min = 1, message = "title must have at least one locale")
    @ValidLocaleKeys
    @MapValueMaxLength(200)
    private Map<String, String> title;

    @ValidLocaleKeys
    @MapValueMaxLength(3000)
    private Map<String, String> description;

    private List<String> techStack;

    @Size(max = 10, message = "links: max 10 items")
    @Valid
    private List<LinkDto> links;

    @Size(max = 10, message = "mediaIds: max 10 items")
    private List<String> mediaIds;

    private Boolean visible = true;

    private Integer order;

    public Map<String, String> getTitle() {
        return title;
    }

    public void setTitle(Map<String, String> title) {
        this.title = title;
    }

    public Map<String, String> getDescription() {
        return description;
    }

    public void setDescription(Map<String, String> description) {
        this.description = description;
    }

    public List<String> getTechStack() {
        return techStack;
    }

    public void setTechStack(List<String> techStack) {
        this.techStack = techStack;
    }

    public List<LinkDto> getLinks() {
        return links;
    }

    public void setLinks(List<LinkDto> links) {
        this.links = links;
    }

    public List<String> getMediaIds() {
        return mediaIds;
    }

    public void setMediaIds(List<String> mediaIds) {
        this.mediaIds = mediaIds;
    }

    public Boolean getVisible() {
        return visible;
    }

    public void setVisible(Boolean visible) {
        this.visible = visible;
    }

    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }
}
