package com.tobyresume.backend.content.experience;

import com.tobyresume.backend.common.model.ContentState;
import com.tobyresume.backend.content.experience.model.WorkExperience;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Work experience collection. At most one document per content state.
 *
 * @see docs/ai/design/database-design.md §5.2, §7
 */
public interface ExperienceRepository extends MongoRepository<WorkExperience, String> {

    WorkExperience findByContentState(ContentState contentState);
}
