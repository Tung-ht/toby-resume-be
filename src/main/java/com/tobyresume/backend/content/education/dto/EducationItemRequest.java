package com.tobyresume.backend.content.education.dto;

import com.tobyresume.backend.common.validation.MapValueMaxLength;
import com.tobyresume.backend.common.validation.ValidLocaleKeys;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.Map;

/**
 * Request body for POST/PUT education item.
 *
 * @see docs/ai/design/api-design.md §4.4, database-design §9
 */
public class EducationItemRequest {

    @NotBlank @Size(max = 200)
    private String institution;
    @NotBlank @Size(max = 200)
    private String degree;
    @Size(max = 200)
    private String field;
    @NotBlank @Pattern(regexp = "^\\d{4}-\\d{2}$", message = "startDate must be YYYY-MM")
    private String startDate;
    @Pattern(regexp = "^\\d{4}-\\d{2}$", message = "endDate must be YYYY-MM")
    private String endDate;
    @ValidLocaleKeys @MapValueMaxLength(1000)
    private Map<String, String> details;
    private Integer order;

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
    public Integer getOrder() { return order; }
    public void setOrder(Integer order) { this.order = order; }
}
