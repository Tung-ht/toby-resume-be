package com.tobyresume.backend.content.skill.dto;

import com.tobyresume.backend.common.validation.MapValueMaxLength;
import com.tobyresume.backend.common.validation.ValidLocaleKeys;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.Map;

/**
 * Request body for POST /skills (add category) and PUT /skills/{categoryId} (update category).
 * Max 50 items per category per database-design §9.
 *
 * @see docs/ai/design/api-design.md §4.5
 */
public class SkillCategoryRequest {

    @NotNull @Size(min = 1, message = "name must have at least one locale")
    @ValidLocaleKeys
    @MapValueMaxLength(100)
    private Map<String, String> name;

    @Valid
    @Size(max = 50, message = "max 50 items per category")
    private List<SkillItemRequest> items;

    private Integer order;

    public Map<String, String> getName() { return name; }
    public void setName(Map<String, String> name) { this.name = name; }
    public List<SkillItemRequest> getItems() { return items; }
    public void setItems(List<SkillItemRequest> items) { this.items = items; }
    public Integer getOrder() { return order; }
    public void setOrder(Integer order) { this.order = order; }
}
