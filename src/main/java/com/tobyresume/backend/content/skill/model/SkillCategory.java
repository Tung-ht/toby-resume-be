package com.tobyresume.backend.content.skill.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Embedded skill category. name is localized; items are SkillItem (name, level).
 *
 * @see docs/ai/design/database-design.md §5.5
 */
public class SkillCategory {

    private String categoryId;
    private Map<String, String> name;
    private List<SkillItem> items = new ArrayList<>();
    private int order;

    public String getCategoryId() { return categoryId; }
    public void setCategoryId(String categoryId) { this.categoryId = categoryId; }
    public Map<String, String> getName() { return name; }
    public void setName(Map<String, String> name) { this.name = name; }
    public List<SkillItem> getItems() { return items; }
    public void setItems(List<SkillItem> items) { this.items = items != null ? items : new ArrayList<>(); }
    public int getOrder() { return order; }
    public void setOrder(int order) { this.order = order; }
}
