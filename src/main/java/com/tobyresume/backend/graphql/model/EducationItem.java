package com.tobyresume.backend.graphql.model;

/**
 * GraphQL type EducationItem — single-locale view.
 */
public class EducationItem {

    private String id;
    private String institution;
    private String degree;
    private String field;
    private String startDate;
    private String endDate;
    private String details;
    private int order;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
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
    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
    public int getOrder() { return order; }
    public void setOrder(int order) { this.order = order; }
}
