package com.tobyresume.backend.content.education.model;

import java.util.Map;

/**
 * Embedded education item. institution, degree, field are plain strings; details is localized.
 *
 * @see docs/ai/design/database-design.md §5.4
 */
public class EducationItem {

    private String itemId;
    private String institution;
    private String degree;
    private String field;
    private String startDate;
    private String endDate;
    private Map<String, String> details;
    private int order;

    public String getItemId() { return itemId; }
    public void setItemId(String itemId) { this.itemId = itemId; }
    public String getInstitution() { return institution; }
    public void setInstitution(String institution) { this.institution = institution; }
    public String getDegree() { return degree; }
    public void setDegree(String degree) { this.degree = degree; }
    public String getField() { return field; }
    public void setField(String field) { this.field = field; }
    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }
    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }
    public Map<String, String> getDetails() { return details; }
    public void setDetails(Map<String, String> details) { this.details = details; }
    public int getOrder() { return order; }
    public void setOrder(int order) { this.order = order; }
}
