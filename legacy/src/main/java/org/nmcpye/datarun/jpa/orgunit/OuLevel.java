package org.nmcpye.datarun.jpa.orgunit;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.nmcpye.datarun.sharedkernal.JpaIdentifiableObject;

/**
 * A OuLevel.
 */
@Entity
@Table(name = "ou_level")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Getter
@Setter
@SuppressWarnings("common-java:DuplicatedBlocks")
public class OuLevel extends JpaIdentifiableObject {

    @Size(max = 11)
    @Column(name = "uid", length = 11, updatable = false, unique = true)
    protected String uid;

    /**
     * The unique code for this object.
     */
    @Column(name = "code", unique = true)
    protected String code;

    /**
     * The name of this object. Required and unique.
     */
    @Column(name = "name", unique = true)
    protected String name;

    @NotNull
    @Column(name = "level", nullable = false)
    private Integer level;

    @Override
    public String toString() {
        return "OuLevel{" +
            "id=" + getId() +
            ", id='" + getUid() + "'" +
            ", name='" + getName() + "'" +
            ", level=" + getLevel() +
            "}";
    }
}
