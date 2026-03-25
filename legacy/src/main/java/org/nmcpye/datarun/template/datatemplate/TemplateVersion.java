package org.nmcpye.datarun.template.datatemplate;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.nmcpye.datarun.template.datatemplateelement.DataOption;
import org.nmcpye.datarun.template.datatemplateelement.FieldTemplateElementDto;
import org.nmcpye.datarun.template.datatemplateelement.SectionTemplateElementDto;
import org.nmcpye.datarun.template.datatemplate.dto.DataTemplateVersionInterface;
import org.nmcpye.datarun.sharedkernal.JpaIdentifiableObject;

import java.util.LinkedList;
import java.util.List;

/**
 * A DataTemplateTemplateVersion.
 */
@Entity
@Table(name = "data_template_version", uniqueConstraints = {
        @UniqueConstraint(name = "ux_tv_id_data_template_id", columnNames = { "id", "data_template_id" }),
        @UniqueConstraint(name = "ux_tv_no_data_template_id", columnNames = { "version_number", "data_template_id" })
}, indexes = {
        @Index(name = "idx_tv_template_uid", columnList = "template_uid"),
        @Index(name = "idx_tv_version_no", columnList = "version_number"),
        @Index(name = "idx_tv_template_uid_version_desc", columnList = "template_uid, version_number"),
})
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Getter
@Setter
@Builder
@AllArgsConstructor
@SuppressWarnings("common-java:DuplicatedBlocks")
public class TemplateVersion extends JpaIdentifiableObject implements DataTemplateVersionInterface {

    @Size(max = 11)
    @Column(name = "uid", length = 11, updatable = false, unique = true, nullable = false)
    protected String uid;

    @NotNull
    @Column(name = "version_number", nullable = false, updatable = false)
    private Integer versionNumber;

    @Column(name = "release_notes", length = 4000)
    private String releaseNotes;

    @JdbcTypeCode(SqlTypes.JSON) // Specifies JSON mapping
    @Column(name = "fields", columnDefinition = "jsonb", nullable = false)
    private List<FieldTemplateElementDto> fields;

    @JdbcTypeCode(SqlTypes.JSON) // Specifies JSON mapping
    @Column(name = "sections", columnDefinition = "jsonb", nullable = false)
    private List<SectionTemplateElementDto> sections;

    @JdbcTypeCode(SqlTypes.JSON) // Specifies JSON mapping
    @Column(name = "options", columnDefinition = "jsonb")
    @Builder.Default
    private List<DataOption> options = new LinkedList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "data_template_id", nullable = false)
    private DataTemplate dataTemplate;

    // legacy DataTemplate.uid
    @Size(max = 11)
    @Column(name = "template_uid", length = 11, updatable = false)
    private String templateUid;

    @Override
    public String getId() {
        return super.getId();
    }

    @Override
    public void setId(String id) {
        super.setId(id);
    }

    public TemplateVersion() {
        setAutoFields();
    }

    public TemplateVersion uid(String uid) {
        this.setUid(uid);
        return this;
    }

    public TemplateVersion version(Integer version) {
        this.setVersionNumber(version);
        return this;
    }

    public TemplateVersion templateUid(String templateUid) {
        this.setTemplateUid(templateUid);
        return this;
    }

    @JsonIgnore
    @Override
    public String getCode() {
        return null;
    }

    @JsonIgnore
    @Override
    public String getName() {
        return null;
    }

    @Override
    public DataTemplateVersionInterface sections(List<SectionTemplateElementDto> sections) {
        this.sections = sections;
        return this;
    }

    @Override
    public DataTemplateVersionInterface fields(List<FieldTemplateElementDto> fields) {
        this.fields = fields;
        return this;
    }
}
