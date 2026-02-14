package com.tobyresume.backend.graphql.model;

/**
 * GraphQL type CertificationItem — single-locale description.
 */
public class CertificationItem {

    private String id;
    private String title;
    private String issuer;
    private String date;
    private String url;
    private String description;
    private int order;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getIssuer() { return issuer; }
    public void setIssuer(String issuer) { this.issuer = issuer; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public int getOrder() { return order; }
    public void setOrder(int order) { this.order = order; }
}
