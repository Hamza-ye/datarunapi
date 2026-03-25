package org.nmcpye.datarun.template.datatemplategenerator;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "canonical_element_config", schema = "analytics")
public class CanonicalElementConfig {
    @Id
    @Column(name = "canonical_element_id", nullable = false)
    private UUID id;

    @Column(name = "template_uid")
    private String templateUid;

    @Column(name = "safe_name")
    private String safeName;

    @Column(name = "safe_name_override")
    private String safeNameOverride;

    @Column(name = "valid_to")
    private Instant validTo;

    @ColumnDefault("false")
    @Column(name = "disabled")
    private Boolean disabled;

    @NotNull
    @ColumnDefault("false")
    @Column(name = "explode", nullable = false)
    private Boolean explode = false;

    @NotNull
    @ColumnDefault("false")
    @Column(name = "anchor_allowed", nullable = false)
    private Boolean anchorAllowed = false;

    @Column(name = "anchor_priority")
    private Integer anchorPriority;

    @NotNull
    @ColumnDefault("now()")
    @Column(name = "updated_date", nullable = false)
    private Instant updatedDate;

}
