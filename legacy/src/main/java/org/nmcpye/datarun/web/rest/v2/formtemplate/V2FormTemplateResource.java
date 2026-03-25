package org.nmcpye.datarun.web.rest.v2.formtemplate;

import org.nmcpye.datarun.acl.AclService;
import org.nmcpye.datarun.template.datatemplate.dto.DataTemplateInstanceDto;
import org.nmcpye.datarun.template.datatemplate.service.DataTemplateInstanceService;
import org.nmcpye.datarun.iam.security.AuthoritiesConstants;
import org.nmcpye.datarun.iam.security.CurrentUserDetails;
import org.nmcpye.datarun.iam.security.SecurityUtils;
import org.nmcpye.datarun.web.rest.v2.formtemplate.service.TemplateTreeTransformer;
import org.nmcpye.datarun.web.common.ApiVersion;
import org.nmcpye.datarun.web.rest.v2.dto.TemplateTreeNode;
import org.nmcpye.datarun.web.rest.v2.dto.V2TemplateTreeDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * V2 REST controller for form templates.
 * <p>
 * Returns a nested template tree with V2 rules (JsonLogic AST).
 * Uses the same underlying {@link DataTemplateInstanceService} as V1,
 * but transforms the output through {@link TemplateTreeTransformer}.
 *
 * @author Hamza Assada
 */
@RestController
@RequestMapping(ApiVersion.API_V2 + "/formTemplates")
@PreAuthorize("hasAnyAuthority(\"" + AuthoritiesConstants.ADMIN + "\", \"" + AuthoritiesConstants.USER + "\")")
public class V2FormTemplateResource {

    private static final Logger log = LoggerFactory.getLogger(V2FormTemplateResource.class);

    private final DataTemplateInstanceService templateService;
    private final AclService aclService;

    public V2FormTemplateResource(DataTemplateInstanceService templateService,
            AclService aclService) {
        this.templateService = templateService;
        this.aclService = aclService;
    }

    /**
     * {@code GET /api/v2/formTemplates/{uid}} : get a template as a V2 tree.
     *
     * @param uid the template UID
     * @return V2TemplateTreeDto with nested tree and transformed rules
     */
    @GetMapping("/{uid}")
    public ResponseEntity<V2TemplateTreeDto> getTemplateTree(@PathVariable("uid") String uid) {
        CurrentUserDetails user = SecurityUtils.getCurrentUserDetailsOrThrow();
        if (!aclService.hasMinimalRights(user)) {
            throw new AccessDeniedException("Insufficient permissions");
        }

        log.debug("V2 REST request to get form template tree: {}", uid);

        return templateService.findByUid(uid)
                .map(this::toV2Tree)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    private V2TemplateTreeDto toV2Tree(DataTemplateInstanceDto dto) {
        TemplateTreeNode root = TemplateTreeTransformer.transform(
                dto.getSections(), dto.getFields());

        return V2TemplateTreeDto.builder()
                .templateUid(dto.getUid())
                .versionUid(dto.getVersionUid())
                .versionNumber(dto.getVersionNumber())
                .name(dto.getName())
                .label(dto.getLabel())
                .root(root)
                .build();
    }
}
