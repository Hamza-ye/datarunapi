package org.nmcpye.datarun.sharedkernal;

import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

/**
 * Base abstract class for entities which will hold definitions for created,
 * last modified, created by,
 * last modified by attributes.
 *
 * @author Hamza Assada
 * @since 20/03/2025
 */
@MappedSuperclass
@Getter
@Setter
public abstract class JpaSoftDeleteObject extends JpaIdentifiableObject implements SoftDeleteObject<String> {

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        JpaSoftDeleteObject that = (JpaSoftDeleteObject) o;
        return getDeleted() == that.getDeleted();
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getDeleted());
    }
}
