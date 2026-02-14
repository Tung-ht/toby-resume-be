package com.tobyresume.backend.content.experience.model;

import com.tobyresume.backend.common.model.BaseDocument;
import com.tobyresume.backend.common.model.ContentState;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

/**
 * Work experience section. One document per content state (DRAFT, PUBLISHED); items embedded.
 *
 * @see docs/ai/design/database-design.md §5.2
 */
@Document(collection = "work_experiences")
public class WorkExperience extends BaseDocument {

    @Indexed(unique = true)
    private ContentState contentState;
    private List<ExperienceItem> items = new ArrayList<>();

    public ContentState getContentState() {
        return contentState;
    }

    public void setContentState(ContentState contentState) {
        this.contentState = contentState;
    }

    public List<ExperienceItem> getItems() {
        return items;
    }

    public void setItems(List<ExperienceItem> items) {
        this.items = items != null ? items : new ArrayList<>();
    }
}
