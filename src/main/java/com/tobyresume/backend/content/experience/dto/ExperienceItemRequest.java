package com.tobyresume.backend.content.experience.dto;

import com.tobyresume.backend.common.validation.MapValueMaxLength;
import com.tobyresume.backend.common.validation.ValidBulletPoints;
import com.tobyresume.backend.common.validation.ValidLocaleKeys;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.Map;

/**
 * Request body for POST /api/v1/experiences and PUT /api/v1/experiences/{itemId}.
 * itemId is assigned by the server on POST.
 *
 * @see docs/ai/design/api-design.md §4.2, database-design §9
 */
public class ExperienceItemRequest {

    @NotNull(message = "company is required")
    @Size(min = 1, message = "company must have at least one locale")
    @ValidLocaleKeys
    @MapValueMaxLength(200)
    private Map<String, String> company;

    @NotNull(message = "role is required")
    @Size(min = 1, message = "role must have at least one locale")
    @ValidLocaleKeys
    @MapValueMaxLength(200)
    private Map<String, String> role;

    @NotBlank(message = "startDate is required")
    @Pattern(regexp = "^\\d{4}-\\d{2}$", message = "startDate must be YYYY-MM")
    private String startDate;

    @Pattern(regexp = "^\\d{4}-\\d{2}$", message = "endDate must be YYYY-MM")
    private String endDate;

    @ValidLocaleKeys
    @ValidBulletPoints
    private Map<String, List<String>> bulletPoints;

    private List<String> techUsed;

    private Integer order;

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

    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }
}
