package com.tobyresume.backend.content.skill.dto;

import java.util.List;
import java.util.Map;

public class SkillCategoryResponse {

    private String categoryId;
    private Map<String, String> name;
    private List<SkillItemResponse> items;
    private int order;

    public String getCategoryId() { return categoryId; }
    public void setCategoryId(String categoryId) { this.categoryId = categoryId; }
    public Map<String, String> getName() { return name; }
    public void setName(Map<String, String> name) { this.name = name; }
    public List<SkillItemResponse> getItems() { return items; }
    public void setItems(List<SkillItemResponse> items) { this.items = items; }
    public int getOrder() { return order; }
    public void setOrder(int order) { this.order = order; }
}
