package com.tobyresume.backend.content.education.model;

import com.tobyresume.backend.common.model.BaseDocument;
import com.tobyresume.backend.common.model.ContentState;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

/**
 * Education section. One document per content state; items embedded.
 *
 * @see docs/ai/design/database-design.md §5.4
 */
@Document(collection = "education")
public class Education extends BaseDocument {

    @Indexed(unique = true)
    private ContentState contentState;
    private List<EducationItem> items = new ArrayList<>();

    public ContentState getContentState() { return contentState; }
    public void setContentState(ContentState contentState) { this.contentState = contentState; }
    public List<EducationItem> getItems() { return items; }
    public void setItems(List<EducationItem> items) { this.items = items != null ? items : new ArrayList<>(); }
}
