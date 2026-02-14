package com.tobyresume.backend.content.sociallink.model;

import com.tobyresume.backend.common.model.BaseDocument;
import com.tobyresume.backend.common.model.ContentState;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document(collection = "social_links")
public class SocialLink extends BaseDocument {

    @Indexed(unique = true)
    private ContentState contentState;
    private List<SocialLinkItem> items = new ArrayList<>();

    public ContentState getContentState() { return contentState; }
    public void setContentState(ContentState contentState) { this.contentState = contentState; }
    public List<SocialLinkItem> getItems() { return items; }
    public void setItems(List<SocialLinkItem> items) { this.items = items != null ? items : new ArrayList<>(); }
}
