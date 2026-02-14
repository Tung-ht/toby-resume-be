package com.tobyresume.backend.content.sociallink.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Request body for POST/PUT social link item.
 *
 * @see docs/ai/design/api-design.md §4.7, database-design §9
 */
public class SocialLinkItemRequest {

    @NotBlank @Size(max = 50)
    private String platform;
    @NotBlank @Size(max = 2048)
    @Pattern(regexp = "^(https?|ftp|mailto):.*$", message = "url must be a valid URL")
    private String url;
    @Size(max = 200)
    private String icon;
    private Integer order;

    public String getPlatform() { return platform; }
    public void setPlatform(String platform) { this.platform = platform; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }
    public Integer getOrder() { return order; }
    public void setOrder(Integer order) { this.order = order; }
}
