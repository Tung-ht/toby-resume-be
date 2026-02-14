package com.tobyresume.backend.content.sociallink;

import com.tobyresume.backend.common.model.ContentState;
import com.tobyresume.backend.content.sociallink.model.SocialLink;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SocialLinkRepository extends MongoRepository<SocialLink, String> {

    SocialLink findByContentState(ContentState contentState);
}
