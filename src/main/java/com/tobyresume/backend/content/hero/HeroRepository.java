package com.tobyresume.backend.content.hero;

import com.tobyresume.backend.common.model.ContentState;
import com.tobyresume.backend.content.hero.model.Hero;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Hero collection. At most one document per content state.
 *
 * @see docs/ai/design/database-design.md §5.1, §7
 */
public interface HeroRepository extends MongoRepository<Hero, String> {

    Hero findByContentState(ContentState contentState);
}
