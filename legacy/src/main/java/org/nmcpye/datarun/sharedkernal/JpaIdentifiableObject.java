package org.nmcpye.datarun.sharedkernal;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import org.nmcpye.datarun.sharedkernal.uidgenerate.CodeGenerator;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.domain.Persistable;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
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
@EntityListeners(AuditingEntityListener.class)
@JsonIgnoreProperties(value = { "new", "createdBy", "createdDate", "lastModifiedBy",
        "lastModifiedDate" }, allowGetters = true)
@Getter
@Setter
public abstract class JpaIdentifiableObject
        implements IdentifiableObject<String>, Persistable<String>, Serializable,
        Comparable<JpaIdentifiableObject> {
    /**
     * ULID id (Universally Unique Lexicographically Sortable Identifier).
     * Length: 26 characters, Base32 encoded.
     */
    @Id
    @NotNull
    @Column(name = "id", length = 26, updatable = false, nullable = false)
    protected String id;

    @Serial
    private static final long serialVersionUID = 1L;

    @CreatedBy
    @Column(name = "created_by", nullable = false, length = 50, updatable = false)
    protected String createdBy;

    @LastModifiedBy
    @Column(name = "last_modified_by", length = 50)
    protected String lastModifiedBy;

    @CreatedDate
    @Column(name = "created_date", updatable = false)
    protected Instant createdDate = Instant.now();

    @LastModifiedDate
    @Column(name = "last_modified_date")
    protected Instant lastModifiedDate = Instant.now();

    @Transient
    protected boolean isPersisted;

    public JpaIdentifiableObject() {
        setAutoFields();
    }

    /**
     * Set auto-generated fields on save or update
     */
    public void setAutoFields() {
        if (this.getId() == null || getUid().isEmpty()) {
            setId(CodeGenerator.nextUlid());
        }
        if (getUid() == null || getUid().isEmpty()) {
            setUid(CodeGenerator.generateUid());
        }
    }

    @PostLoad
    @PostPersist
    protected void updateEntityState() {
        this.setIsPersisted();
    }

    @JsonIgnore
    public boolean isPersisted() {
        return isPersisted;
    }

    @Transient
    @Override
    @JsonIgnore
    public boolean isNew() {
        return !this.isPersisted;
    }

    public void setIsPersisted() {
        this.isPersisted = true;
    }

    @SuppressWarnings("unchecked")
    public <T extends JpaIdentifiableObject> T persisted() {
        this.isPersisted = true;
        return (T) this;
    }

    abstract protected void setUid(String uid);

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }

        if (!getClass().isAssignableFrom(o.getClass())) {
            return false;
        }

        if (!(o instanceof JpaIdentifiableObject that))
            return false;
        return Objects.equals(getId(), that.getId());
    }

    @Override
    public int compareTo(JpaIdentifiableObject object) {
        if (this.getName() == null) {
            return object.getName() == null ? 0 : 1;
        }

        return object.getName() == null ? -1
                : this.getName().compareToIgnoreCase(object.getName());
    }

    @Override
    public String getPropertyValue(IdScheme idScheme) {
        if (idScheme.isNull() || idScheme.is(IdentifiableProperty.ID)) {
            return getId();
        } else if (idScheme.is(IdentifiableProperty.CODE)) {
            return getCode();
        } else if (idScheme.is(IdentifiableProperty.NAME)) {
            return getName();
        }

        return null;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }
}
