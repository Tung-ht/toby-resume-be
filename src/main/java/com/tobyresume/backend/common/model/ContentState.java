package com.tobyresume.backend.common.model;

/**
 * Content lifecycle state. Each content collection holds at most one document per state.
 * Stored as string in MongoDB: "DRAFT" or "PUBLISHED".
 *
 * @see docs/ai/design/database-design.md §4.1
 */
public enum ContentState {
    DRAFT,
    PUBLISHED
}
