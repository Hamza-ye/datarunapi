package org.nmcpye.datarun.jpa.option;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Type;
import org.nmcpye.datarun.sharedkernal.JpaIdentifiableObject;
import org.nmcpye.datarun.sharedkernal.TranslatableInterface;
import org.nmcpye.datarun.sharedkernal.translation.Translation;

import java.time.Instant;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Hamza Assada
 * @since 30/06/2025
 */
@Entity
@Table(name = "option_value")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Getter
@Setter
@SQLDelete(sql = "UPDATE option_value SET deleted_at = now() WHERE id = ?")
public class Option extends JpaIdentifiableObject implements TranslatableInterface {
    @Size(max = 11)
    @Column(name = "uid", length = 11, updatable = false, unique = true)
    protected String uid;

    /**
     * The unique code for this object.
     */
    @Column(name = "code", nullable = false, updatable = false)
    private String code;

    /**
     * The name of this object. Required and unique.
     */
    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    // only getter, setting is done by jpa
    @Setter(value = AccessLevel.PRIVATE)
    @Column(name = "sort_order")
    private Integer sortOrder;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "option_set_id", nullable = false, updatable = false)
    private OptionSet optionSet;

    @Type(JsonType.class)
    @Column(name = "properties_map", columnDefinition = "jsonb")
    @JsonProperty
    protected Map<String, Object> properties;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    /**
     * Set of available object translation, normally filtered by locale.
     */
    @Type(JsonType.class)
    @Column(name = "translations", columnDefinition = "jsonb")
    protected Set<Translation> translations = new HashSet<>();

    public boolean isDeleted() {
        return deletedAt != null;
    }

    public void setDeleted(boolean deleted) {
        deletedAt = deleted ? Instant.now() : null;
    }

    @JsonProperty
    @JsonSerialize(as = JpaIdentifiableObject.class)
    public OptionSet getOptionSet() {
        return optionSet;
    }
}
