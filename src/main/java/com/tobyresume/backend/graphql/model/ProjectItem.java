package com.tobyresume.backend.graphql.model;

import java.util.List;

/**
 * GraphQL type ProjectItem — single-locale view; mediaUrls are IDs until media service exists.
 */
public class ProjectItem {

    private String id;
    private String title;
    private String description;
    private List<String> techStack;
    private List<Link> links;
    private List<String> mediaUrls;
    private boolean visible;
    private int order;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public List<String> getTechStack() { return techStack; }
    public void setTechStack(List<String> techStack) { this.techStack = techStack; }
    public List<Link> getLinks() { return links; }
    public void setLinks(List<Link> links) { this.links = links; }
    public List<String> getMediaUrls() { return mediaUrls; }
    public void setMediaUrls(List<String> mediaUrls) { this.mediaUrls = mediaUrls; }
    public boolean isVisible() { return visible; }
    public void setVisible(boolean visible) { this.visible = visible; }
    public int getOrder() { return order; }
    public void setOrder(int order) { this.order = order; }
}
