package com.tobyresume.backend.content.certification.model;

import java.util.Map;

/**
 * Embedded certification item. title, issuer are plain strings; description is localized.
 *
 * @see docs/ai/design/database-design.md §5.6
 */
public class CertificationItem {

    private String itemId;
    private String title;
    private String issuer;
    private String date;
    private String url;
    private Map<String, String> description;
    private int order;

    public String getItemId() { return itemId; }
    public void setItemId(String itemId) { this.itemId = itemId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getIssuer() { return issuer; }
    public void setIssuer(String issuer) { this.issuer = issuer; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public Map<String, String> getDescription() { return description; }
    public void setDescription(Map<String, String> description) { this.description = description; }
    public int getOrder() { return order; }
    public void setOrder(int order) { this.order = order; }
}
