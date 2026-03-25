package org.nmcpye.datarun.web.rest.v1.formtemplate.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import org.nmcpye.datarun.template.datatemplateelement.DataOption;
import org.nmcpye.datarun.template.datatemplateelement.FieldTemplateElementDto;
import org.nmcpye.datarun.template.datatemplateelement.SectionTemplateElementDto;

import java.time.Instant;
import java.util.List;

/**
 * Frozen DTO representing the v1 TemplateVersion structure
 * as consumed by the mobile app. Read-only contract.
 * The JSONB columns (fields, sections, options) are preserved as-is.
 */
@Getter
@Setter
public class TemplateVersionV1Dto {

    private String uid;

    private Integer versionNumber;

    private String releaseNotes;

    private List<FieldTemplateElementDto> fields;

    private List<SectionTemplateElementDto> sections;

    private List<DataOption> options;

    @JsonProperty("templateUid")
    private String templateUid;

    private String createdBy;

    private Instant createdDate;

    private String lastModifiedBy;

    private Instant lastModifiedDate;
}
