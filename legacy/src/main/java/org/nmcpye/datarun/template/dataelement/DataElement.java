package org.nmcpye.datarun.template.dataelement;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Type;
import org.nmcpye.datarun.template.datatemplateelement.enumeration.ReferenceType;
import org.nmcpye.datarun.template.datatemplateelement.enumeration.ValueType;
import org.nmcpye.datarun.jpa.option.OptionSet;
import org.nmcpye.datarun.sharedkernal.JpaIdentifiableObject;
import org.nmcpye.datarun.sharedkernal.TranslatableInterface;
import org.nmcpye.datarun.sharedkernal.translation.Translation;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Hamza Assada
 * @since 08/02/2024
 */
@Entity
@Table(name = "data_element")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Getter
@Setter
@SuppressWarnings("common-java:DuplicatedBlocks")
public class DataElement extends JpaIdentifiableObject implements TranslatableInterface {
    @Size(max = 11)
    @Column(name = "uid", length = 11, updatable = false, unique = true)
    protected String uid;
    /**
     * The code for this Element.
     * unique, but not required
     */
    @Column(name = "code", unique = true)
    protected String code;

    /**
     * The name of this data element.
     * Required and unique with no spaces.
     */
    @Column(name = "name", nullable = false, unique = true)
    protected String name;

    @Column(name = "short_name", length = 50)
    protected String shortName;

    @Size(max = 2000)
    @Column(name = "description")
    protected String description;

    /**
     * Type of Value (e.g, Text, Number, Integer, OrgUnit, Entity, Team, Date,
     * Coordinates)
     */
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "type", updatable = false, nullable = false)
    @JsonProperty(value = "type")
    protected ValueType valueType;

    /**
     * an option set groups a predefined JSONP List of options, used when
     * the dataType is of type `Select` or =null otherwise.
     */
    @ManyToOne
    @JsonIgnoreProperties(value = { "options" }, allowSetters = true)
    @JoinColumn(updatable = false)
    protected OptionSet optionSet;

    /**
     * resourceType for ReferenceField type
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "reference_type", updatable = false)
    private ReferenceType resourceType;

    /**
     * Set of available object translation, normally filtered by locale.
     */
    @Type(JsonType.class)
    @Column(name = "translations", columnDefinition = "jsonb")
    protected Set<Translation> translations = new HashSet<>();

}
