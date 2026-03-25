package org.nmcpye.datarun.web.rest.v2.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;

/**
 * Top-level response DTO for {@code GET /api/v2/formTemplates/{uid}}.
 * Wraps the template tree with template-level metadata.
 *
 * @author Hamza Assada
 */
@Getter
@Builder
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class V2TemplateTreeDto {

    private final String templateUid;
    private final String versionUid;
    private final Integer versionNumber;
    private final String name;
    private final Map<String, String> label;

    /**
     * The nested template tree. Root node with type "root".
     */
    private final TemplateTreeNode root;
}
