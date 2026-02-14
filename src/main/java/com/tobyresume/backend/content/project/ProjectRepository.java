package com.tobyresume.backend.content.project;

import com.tobyresume.backend.common.model.ContentState;
import com.tobyresume.backend.content.project.model.Project;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Projects collection. At most one document per content state.
 *
 * @see docs/ai/design/database-design.md §5.3, §7
 */
public interface ProjectRepository extends MongoRepository<Project, String> {

    Project findByContentState(ContentState contentState);
}
