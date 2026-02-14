package com.tobyresume.backend.content.skill.model;

import com.tobyresume.backend.common.model.BaseDocument;
import com.tobyresume.backend.common.model.ContentState;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document(collection = "skills")
public class Skill extends BaseDocument {

    @Indexed(unique = true)
    private ContentState contentState;
    private List<SkillCategory> categories = new ArrayList<>();

    public ContentState getContentState() { return contentState; }
    public void setContentState(ContentState contentState) { this.contentState = contentState; }
    public List<SkillCategory> getCategories() { return categories; }
    public void setCategories(List<SkillCategory> categories) { this.categories = categories != null ? categories : new ArrayList<>(); }
}
