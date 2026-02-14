package com.tobyresume.backend.graphql.model;

import java.util.List;

/**
 * GraphQL type SkillCategory — single-locale name, items as-is.
 */
public class SkillCategory {

    private String id;
    private String name;
    private List<SkillItem> items;
    private int order;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public List<SkillItem> getItems() { return items; }
    public void setItems(List<SkillItem> items) { this.items = items; }
    public int getOrder() { return order; }
    public void setOrder(int order) { this.order = order; }
}
