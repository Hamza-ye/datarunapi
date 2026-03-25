package org.nmcpye.datarun.jpa.option;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.ListIndexBase;
import org.hibernate.annotations.Type;
import org.nmcpye.datarun.sharedkernal.JpaIdentifiableObject;
import org.nmcpye.datarun.sharedkernal.TranslatableInterface;
import org.nmcpye.datarun.sharedkernal.translation.Translation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Hamza Assada
 * @since 30/06/2025
 */
@Entity
@Table(name = "option_group_set", uniqueConstraints = {
        @UniqueConstraint(name = "uc_group_set_option_set_name", columnNames = { "name", "option_set_id" }),
        @UniqueConstraint(name = "uc_group_set_option_set_code", columnNames = { "code", "option_set_id" }),
})
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Getter
@Setter
@SuppressWarnings("common-java:DuplicatedBlocks")
public class OptionGroupSet extends JpaIdentifiableObject implements TranslatableInterface {
    /**
     * The unique code for this object.
     */
    @Column(name = "code")
    private String code;

    /**
     * The name of this object. Required and unique.
     */
    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    /**
     * when set you cannot change this association later
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "option_set_id", updatable = false, nullable = false)
    private OptionSet optionSet;

    @ManyToMany
    @JoinTable(name = "option_groupset__option_group", joinColumns = @JoinColumn(name = "groupset_id"), inverseJoinColumns = @JoinColumn(name = "option_group_id"))
    @OrderColumn(name = "sort_order")
    @ListIndexBase(1)
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    private List<OptionGroup> optionGroups = new ArrayList<>();

    /**
     * Set of available object translation, normally filtered by locale.
     */
    @Type(JsonType.class)
    @Column(name = "translations", columnDefinition = "jsonb")
    protected Set<Translation> translations = new HashSet<>();

    @JsonProperty
    @JsonSerialize(contentAs = JpaIdentifiableObject.class)
    public List<OptionGroup> getOptionGroups() {
        return optionGroups;
    }

    @JsonSerialize(as = JpaIdentifiableObject.class)
    public OptionSet getOptionSet() {
        return optionSet;
    }

    @Override
    protected void setUid(String uid) {

    }

    @JsonIgnore
    @Override
    public String getUid() {
        return null;
    }
}
