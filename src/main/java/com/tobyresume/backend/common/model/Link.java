package com.tobyresume.backend.common.model;

/**
 * Embedded link (label + url). Used in ProjectItem.
 *
 * @see docs/ai/design/database-design.md §4.3
 */
public class Link {

    private String label;
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
