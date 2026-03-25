package org.nmcpye.datarun.template.datatemplate;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Type;
import org.nmcpye.datarun.sharedkernal.uidgenerate.CodeGenerator;

import java.time.Instant;
import java.util.Map;
import java.util.Set;

/// Template Element (a node in a form template): A template's element Definition Registry, it is an
/// "Immutable Blueprint", Snapshotting an element per template version saved Versioned, can be a repeat element,
/// or a field element, not sections, Sections are not saved and are "visual-only",
/// and only appear in the [TemplateElement#jsonDataPath].
///
/// @author Hamza
/// @since 18/08/2025
@Entity
@Table(name = "template_element", uniqueConstraints = {
        @UniqueConstraint(name = "ux_template_element_tpl_ver_idpath", columnNames = { "template_uid",
                "template_version_uid", "canonical_path" }),
        @UniqueConstraint(name = "ux_template_element_tpl_ver_json_path", columnNames = { "template_uid",
                "template_version_uid", "json_data_path" })
}, indexes = {
        @Index(name = "idx_template_element_template_version", columnList = "template_uid, template_version_uid"),
        @Index(name = "idx_template_element_template_version_no", columnList = "template_uid, template_version_no"),
        @Index(name = "idx_template_element_canonical_uid", columnList = "canonical_element_id"),
        @Index(name = "idx_template_element_repeat_path", columnList = "template_uid, parent_repeat_json_data_path")
})
@AllArgsConstructor
@Getter
@Setter
@Builder
@ToString(onlyExplicitlyIncluded = true)
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class TemplateElement {
    /// used only internally, for relations and db, uid is the one used externally
    /// and in analytics
    /// to uniquely-across-the-system identify an element
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "template_element_seq")
    @SequenceGenerator(name = "template_element_seq", sequenceName = "template_element_seq", allocationSize = 1)
    private Long id;

    /// The UI Pointer (Internal to the Form Engine)
    @Size(max = 11)
    @Column(name = "uid", length = 64, updatable = false, unique = true, nullable = false)
    protected String uid;

    /// uid of the [DataTemplate] containing this template element.
    @NotNull
    @Column(name = "template_uid", length = 11, nullable = false)
    @ToString.Include
    private String templateUid;

    /// uid of the [TemplateVersion] of the [DataTemplate].
    @NotNull
    @Column(name = "template_version_uid", length = 11, nullable = false)
    @ToString.Include
    private String templateVersionUid;

    /// Version number, denormalized from the [TemplateVersion] of the
    /// [DataTemplate]
    /// for version sorting.
    @NotNull
    @Column(name = "template_version_no", nullable = false)
    private Integer templateVersionNo;

    /// globally immutable unique uid of the [DataElement] being configured.
    @NotNull
    @Deprecated
    @Column(name = "data_element_uid", length = 100)
    private String dataElementUid;

    /// Used as the scoped key in the structured submission data.
    /// uniqueness of name is scoped per level: local repeat namespace, or global
    /// root level,
    /// moving an element around sections in same level doesn't change this fact.
    @NotNull
    @Column(name = "name", columnDefinition = "text", nullable = false)
    private String name;

    /// Option set uid
    /// [#SelectMulti] field.
    @Column(name = "option_set_uid", length = 11)
    private String optionSetUid;

    /// Option set uid
    /// [#SelectMulti] field.
    @Column(name = "option_set_id", length = 26)
    private String optionSetId;

    @Column(name = "sort_order")
    private Integer sortOrder;

    /// the element path (remains stable in same version),
    /// changes when element is moved across different sections, or repeats.
    /// **Example:** `patient.medications[*].name``
    @Column(name = "json_data_path", length = 3000, nullable = false)
    private String jsonDataPath;

    /// The Canonical path. (remains stable across versions and maintain the
    /// template's global identity of an element
    /// across different template's versions.
    /// Changes only when an element change it's global identity that is only
    /// effected when an element is moved
    /// across levels that include repeats.
    ///
    /// **Example**:
    ///
    /// The full `repeat_path` (e.g., `root.householdinfo.children`) mixes two
    /// different concepts:
    /// 1. **Structural Grouping:** The `householdinfo` part is just a visual
    /// grouping on the form. An admin
    /// could rename it to `household_details` tomorrow, and it would mean the exact
    /// same thing to the user.
    /// 2. **Data Grain:** The `children` part, because it is `repeatable: true`,
    /// defines a fundamental change
    /// in the global identity.
    @Column(name = "canonical_path", length = 3000)
    private String canonicalPath;

    /// nearest repeat ancestor's path
    @Column(name = "parent_repeat_canonical_path", length = 3000)
    private String parentRepeatCanonicalPath;

    /// the [#jsonDataPath] of the nearest repeatable ancestor (or null), dot
    /// delimited. no array [*]
    @Column(name = "parent_repeat_json_data_path", length = 3000)
    private String parentRepeatJsonDataPath;

    /// Localized display labels for ui only, e.g. `{"en": "Child Name", "ar":
    /// "..."}`.
    @Type(JsonType.class)
    @Column(name = "display_label", columnDefinition = "jsonb default '{}'::jsonb")
    private Map<String, String> displayLabel;

    /// the element's value data type e.g. TEXT, BOOLEAN, INTEGER, DECIMAL,
    /// TIMESTAMP, ARRAY
    @Enumerated(EnumType.STRING)
    @Column(name = "data_type", length = 64)
    private DataType dataType;

    /// the element's semantical meaning or shape, a hint
    /// e.g. OrgUnit, Team, Activity, Option, MultiSelectOption, Repeat, Name
    /// (person full name), Age, PhoneNumber,
    /// Email, URL, Username, Coordinate, GeoJson
    @Enumerated(EnumType.STRING)
    @Column(name = "semantic_type", length = 64)
    private SemanticType semanticType;

    /// canonicalElementId, canonical attributes of an element are stored in
    /// [CanonicalElement],
    /// remains stable, unless the canonical path or a canonical attributes are
    /// changed,
    /// a new canonical element is created. a canonical element might point to one
    /// or more
    /// across template's versions [TemplateElement].
    ///
    /// **Among an element's attributes that currently considered canonical are:**
    /// 1. [TemplateElement#canonicalPath]
    /// 2. [TemplateElement#dataType]
    @Column(name = "canonical_element_id", nullable = false)
    private String canonicalElementId;

    /// If this is a repeat, what's the group of elements that compose a natural key
    /// for its instance level
    ///
    /// @deprecated the decision of a natural keys is currently decided and managed
    /// by a processing layer.
    @Deprecated
    @Type(JsonType.class)
    @Singular
    @Column(name = "natural_key_candidates", columnDefinition = "jsonb default '[]'::jsonb")
    private Set<String> naturalKeyCandidates;

    /// Timestamp of creation.
    @NotNull
    @Column(name = "created_date", nullable = false, updatable = false)
    private Instant createdDate;

    public boolean isRepeat() {
        return this.semanticType == SemanticType.Repeat;
    }

    public TemplateElement() {
        setAutoFields();
    }

    public void setAutoFields() {
        if (createdDate == null)
            createdDate = Instant.now();
        if (getUid() == null || getUid().isEmpty()) {
            setUid(CodeGenerator.generateUid());
        }
    }
}
