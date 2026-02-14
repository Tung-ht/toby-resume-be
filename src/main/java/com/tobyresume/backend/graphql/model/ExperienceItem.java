package com.tobyresume.backend.graphql.model;

import java.util.List;

/**
 * GraphQL type ExperienceItem — single-locale view.
 */
public class ExperienceItem {

    private String id;
    private String company;
    private String role;
    private String startDate;
    private String endDate;
    private List<String> bulletPoints;
    private List<String> techUsed;
    private int order;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }
    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }
    public List<String> getBulletPoints() { return bulletPoints; }
    public void setBulletPoints(List<String> bulletPoints) { this.bulletPoints = bulletPoints; }
    public List<String> getTechUsed() { return techUsed; }
    public void setTechUsed(List<String> techUsed) { this.techUsed = techUsed; }
    public int getOrder() { return order; }
    public void setOrder(int order) { this.order = order; }
}
