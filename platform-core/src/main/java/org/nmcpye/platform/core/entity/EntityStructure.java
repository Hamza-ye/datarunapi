package org.nmcpye.platform.core.entity;

/**
 * Defines the structural shape of an EntityType.
 */
public enum EntityStructure {
    /** Flat list of entities, no hierarchy. */
    FLAT,
    /** Tree-structured entities with parent-child relationships. */
    HIERARCHICAL,
    /** Entities organized into groups/sets. */
    GROUPED
}
