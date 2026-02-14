package com.tobyresume.backend.content.experience.model;

import java.util.List;
import java.util.Map;

/**
 * Embedded work experience item. Stored inside a WorkExperience document.
 *
 * @see docs/ai/design/database-design.md §5.2
 */
public class ExperienceItem {

    private String itemId;
    private Map<String, String> company;
    private Map<String, String> role;
    private String startDate;
    private String endDate;
    private Map<String, List<String>> bulletPoints;
    private List<String> techUsed;
    private int order;

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public Map<String, String> getCompany() {
        return company;
    }

    public void setCompany(Map<String, String> company) {
        this.company = company;
    }

    public Map<String, String> getRole() {
        return role;
    }

    public void setRole(Map<String, String> role) {
        this.role = role;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public Map<String, List<String>> getBulletPoints() {
        return bulletPoints;
    }

    public void setBulletPoints(Map<String, List<String>> bulletPoints) {
        this.bulletPoints = bulletPoints;
    }

    public List<String> getTechUsed() {
        return techUsed;
    }

    public void setTechUsed(List<String> techUsed) {
        this.techUsed = techUsed;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }
}
