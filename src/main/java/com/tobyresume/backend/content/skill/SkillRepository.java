package com.tobyresume.backend.content.skill;

import com.tobyresume.backend.common.model.ContentState;
import com.tobyresume.backend.content.skill.model.Skill;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SkillRepository extends MongoRepository<Skill, String> {

    Skill findByContentState(ContentState contentState);
}
