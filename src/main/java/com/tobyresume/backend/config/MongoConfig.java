package com.tobyresume.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

/**
 * Enables MongoDB auditing so that {@link com.tobyresume.backend.common.model.BaseDocument}
 * subclasses get createdAt and updatedAt set automatically.
 *
 * @see docs/ai/design/database-design.md §4.2
 */
@Configuration
@EnableMongoAuditing
public class MongoConfig {
}
