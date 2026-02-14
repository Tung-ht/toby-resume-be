package com.tobyresume.backend.content.education;

import com.tobyresume.backend.common.model.ContentState;
import com.tobyresume.backend.content.education.model.Education;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface EducationRepository extends MongoRepository<Education, String> {

    Education findByContentState(ContentState contentState);
}
