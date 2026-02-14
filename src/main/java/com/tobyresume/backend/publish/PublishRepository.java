package com.tobyresume.backend.publish;

import com.tobyresume.backend.publish.model.VersionSnapshot;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/**
 * Version snapshots: append-only, one document per publish.
 *
 * @see docs/ai/design/database-design.md §5.9, §7
 */
public interface PublishRepository extends MongoRepository<VersionSnapshot, String> {

    /**
     * Latest snapshot for status (last publish time, version count).
     */
    List<VersionSnapshot> findTop1ByOrderByPublishedAtDesc();
}
