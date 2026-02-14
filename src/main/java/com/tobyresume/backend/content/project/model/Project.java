package com.tobyresume.backend.content.project.model;

import com.tobyresume.backend.common.model.BaseDocument;
import com.tobyresume.backend.common.model.ContentState;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

/**
 * Projects/portfolio section. One document per content state; items embedded.
 *
 * @see docs/ai/design/database-design.md §5.3
 */
@Document(collection = "projects")
public class Project extends BaseDocument {

    @Indexed(unique = true)
    private ContentState contentState;
    private List<ProjectItem> items = new ArrayList<>();

    public ContentState getContentState() {
        return contentState;
    }

    public void setContentState(ContentState contentState) {
        this.contentState = contentState;
    }

    public List<ProjectItem> getItems() {
        return items;
    }

    public void setItems(List<ProjectItem> items) {
        this.items = items != null ? items : new ArrayList<>();
    }
}
