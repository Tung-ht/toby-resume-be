package com.tobyresume.backend.graphql.model;

/**
 * GraphQL type SocialLinkItem (no localized fields).
 */
public class SocialLinkItem {

    private String id;
    private String platform;
    private String url;
    private String icon;
    private int order;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getPlatform() { return platform; }
    public void setPlatform(String platform) { this.platform = platform; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }
    public int getOrder() { return order; }
    public void setOrder(int order) { this.order = order; }
}
