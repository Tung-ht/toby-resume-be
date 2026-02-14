package com.tobyresume.backend.content.skill.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class SkillItemRequest {

    @NotBlank @Size(max = 100)
    private String name;
    @Size(max = 50)
    private String level;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }
}
