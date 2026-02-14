package com.tobyresume.backend.content.certification;

import com.tobyresume.backend.common.model.ContentState;
import com.tobyresume.backend.content.certification.model.Certification;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CertificationRepository extends MongoRepository<Certification, String> {

    Certification findByContentState(ContentState contentState);
}
