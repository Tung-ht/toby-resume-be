package com.tobyresume.backend.content.project.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Request/response link (label + url). Validation per database-design §4.3.
 */
public class LinkDto {

    @NotBlank(message = "label is required")
    @Size(max = 100)
    private String label;

    @NotBlank(message = "url is required")
    @Size(max = 2048)
    @Pattern(regexp = "^(https?|ftp)://[^\\s/$.?#].[^\\s]*$", message = "url must be a valid URL")
    private String url;

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
