package org.nmcpye.datarun.template.datatemplate;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import org.hibernate.annotations.Type;

import java.time.Instant;
import java.util.Map;
import java.util.Set;

/**
 * @author Hamza Assada
 * @since 25/09/2025
 */
@Entity
@Table(name = "canonical_element")
@Getter
@Setter
@Accessors(chain = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CanonicalElement {

    @Id
    @Column(name = "id", updatable = false, nullable = false, unique = true)
    private String id;

    @Column(name = "template_uid", updatable = false, nullable = false, length = 11)
    private String templateUid;

    @Column(name = "preferred_name", updatable = false, nullable = false)
    private String preferredName;

    /**
     * deterministic, safe, short name, for pivoting column names.
     */
    @Column(name = "safe_name", updatable = false, nullable = false, length = 63)
    private String safeName;

    @Enumerated(EnumType.STRING)
    @Column(name = "data_type", length = 64)
    private DataType dataType;

    @Enumerated(EnumType.STRING)
    @Column(name = "semantic_type", length = 64)
    private SemanticType semanticType;

    @Type(JsonType.class)
    @Column(name = "canonical_path", length = 3000)
    private String canonicalPath;

    @Column(name = "option_set_uid", length = 11)
    private String optionSetUid;

    @Column(name = "option_set_id", length = 26)
    private String optionSetId;

    /// last updated Localized display labels, e.g. `{"en": "Child Name", "ar": "..."}`.
    @Type(JsonType.class)
    @Column(name = "display_label", columnDefinition = "jsonb default '{}'::jsonb")
    private Map<String, String> displayLabel;

    @Type(JsonType.class)
    @Column(name = "json_data_paths", columnDefinition = "jsonb default '[]'::jsonb")
    private Set<String> jsonDataPaths;

    @Column(name = "parent_repeat_id", columnDefinition = "text")
    private String parentRepeatId;

    @Column(name = "created_date")
    private Instant createdDate;

    @Column(name = "last_modified_date")
    private Instant lastModifiedDate;

    public boolean isRepeatCE() {
        return this.getSemanticType() == SemanticType.Repeat;
    }
}
