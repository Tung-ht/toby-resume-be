package com.tobyresume.backend.content.certification.dto;

import com.tobyresume.backend.common.validation.MapValueMaxLength;
import com.tobyresume.backend.common.validation.ValidLocaleKeys;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.Map;

/**
 * Request body for POST/PUT certification item.
 *
 * @see docs/ai/design/api-design.md §4.6, database-design §9
 */
public class CertificationItemRequest {

    @NotBlank @Size(max = 200)
    private String title;
    @NotBlank @Size(max = 200)
    private String issuer;
    @Pattern(regexp = "^(\\d{4}-\\d{2}(-\\d{2})?)?$", message = "date must be YYYY-MM or YYYY-MM-DD")
    private String date;
    @Size(max = 2048)
    private String url;
    @ValidLocaleKeys @MapValueMaxLength(500)
    private Map<String, String> description;
    private Integer order;

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
    public Integer getOrder() { return order; }
    public void setOrder(Integer order) { this.order = order; }
}
