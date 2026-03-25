package org.nmcpye.datarun.sharedkernal;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.Instant;

/**
 * @author Hamza Assada 20/03/2025 (7amza.it@gmail.com)
 */
public interface IdentifiableObject<ID> {
    ID getId();

    void setId(ID id);

    String getUid();

    String getCode();

    String getName();

    String getCreatedBy();

    void setCreatedBy(String user);

    Instant getCreatedDate();

    String getLastModifiedBy();

    void setLastModifiedBy(String user);

    Instant getLastModifiedDate();

    /**
     * Returns the value of the property referred to by the given IdScheme.
     *
     * @param idScheme the IdScheme.
     * @return the value of the property referred to by the IdScheme.
     */
    @JsonIgnore
    default ID getPropertyValue(IdScheme idScheme) {
        if (idScheme.isNull() || idScheme.is(IdentifiableProperty.ID)) {
            return getId();
        }

        return null;
    }
}
