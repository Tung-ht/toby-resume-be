package com.tobyresume.backend.common.util;

import java.util.UUID;

/**
 * Generates IDs for embedded content items (e.g. experience items, project items).
 * Uses UUID so IDs are URL-safe and unique across sections without MongoDB ObjectId.
 *
 * @see docs/ai/design/database-design.md — UUID for embedded item IDs
 */
public final class IdGenerator {

    private IdGenerator() {
    }

    /**
     * Returns a new UUID string (e.g. "550e8400-e29b-41d4-a716-446655440000").
     */
    public static String uuid() {
        return UUID.randomUUID().toString();
    }
}
