package org.nmcpye.datarun.sharedkernal.enumeration;

/**
 * Defines the highest resolved permission level a user has on a specific
 * operational entity.
 * Used primarily in the UserExecutionContext CQRS view to simplify API
 * filtering without complex hierarchical queries.
 * 
 * @since 09/04/2026
 */
public enum AccessLevel {
    READ,
    UPDATE,
    DELETE,
    ALL
}
