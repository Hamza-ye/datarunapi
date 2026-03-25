package org.nmcpye.datarun.template.datatemplate.service;

import org.nmcpye.datarun.template.datatemplate.TemplateVersion;
import org.nmcpye.datarun.template.datatemplate.dto.FormTemplateVersionDto;
import org.nmcpye.datarun.sharedkernal.JpaIdentifiableObjectService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

/**
 * Service Custom Interface for managing {@link TemplateVersion}.
 */
public interface TemplateVersionService
    extends JpaIdentifiableObjectService<TemplateVersion> {
    Optional<TemplateVersion> findLatestByTemplate(String templateUid);

    FormTemplateVersionDto findByVersion(String masterUid, int version);

    Page<FormTemplateVersionDto> pageVersions(String templateId, Pageable pageable);
}
