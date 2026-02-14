package com.tobyresume.backend.content.sociallink.model;

/**
 * Embedded social link item. No localized fields.
 *
 * @see docs/ai/design/database-design.md §5.7
 */
public class SocialLinkItem {

    private String itemId;
    private String platform;
    private String url;
    private String icon;
    private int order;

    public String getItemId() { return itemId; }
    public void setItemId(String itemId) { this.itemId = itemId; }
    public String getPlatform() { return platform; }
    public void setPlatform(String platform) { this.platform = platform; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }
    public int getOrder() { return order; }
    public void setOrder(int order) { this.order = order; }
}
