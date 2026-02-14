package com.tobyresume.backend.content.skill.model;

/**
 * Nested skill item inside a category. name and level are plain strings.
 *
 * @see docs/ai/design/database-design.md §5.5
 */
public class SkillItem {

    private String name;
    private String level;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }
}
