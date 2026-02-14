package com.tobyresume.backend.content.certification.model;

import com.tobyresume.backend.common.model.BaseDocument;
import com.tobyresume.backend.common.model.ContentState;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document(collection = "certifications")
public class Certification extends BaseDocument {

    @Indexed(unique = true)
    private ContentState contentState;
    private List<CertificationItem> items = new ArrayList<>();

    public ContentState getContentState() { return contentState; }
    public void setContentState(ContentState contentState) { this.contentState = contentState; }
    public List<CertificationItem> getItems() { return items; }
    public void setItems(List<CertificationItem> items) { this.items = items != null ? items : new ArrayList<>(); }
}
