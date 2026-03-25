package org.nmcpye.datarun.sharedkernal;

import java.time.Instant;

/**
 * @author Hamza Assada 20/03/2025 (7amza.it@gmail.com)
 */
public interface SoftDeleteObject<ID> {
    default Boolean getDeleted() {
        return getDeletedAt() != null;
    }

    Instant getDeletedAt();

    void setDeletedAt(Instant deletedAt);

    default void setDeleted(Boolean deleted) {
        if (!getDeleted()) setDeletedAt(Instant.now());
    }
}
