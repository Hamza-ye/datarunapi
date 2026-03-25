package org.nmcpye.datarun.template.datatemplate.repository;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import org.nmcpye.datarun.template.datatemplate.TemplateVersion;
import org.nmcpye.datarun.sharedkernal.JpaIdentifiableRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.*;

/// Spring Data jpa repository for the DataTemplate entity.
@Repository
public interface TemplateVersionRepository
    extends JpaIdentifiableRepository<TemplateVersion> {
    String TEMPLATE_UID_VERSION_NO_JPA_CACHE = "templateUidVersionNoCache";
    String TEMPLATE_UID_VERSION_UID_JPA_CACHE = "templateUidVersionUidCache";
    String TEMPLATE_UID_LATEST_VERSION_JPA_CACHE = "templateUidLatestVersionCache";

    // Returns the single FormInstance with highest version for this template
    @Cacheable(cacheNames = TEMPLATE_UID_LATEST_VERSION_JPA_CACHE)
    Optional<TemplateVersion> findTopByTemplateUidOrderByVersionNumberDesc(String templateId);

    // Returns a specific version, if it exists
    @Cacheable(cacheNames = TEMPLATE_UID_VERSION_NO_JPA_CACHE)
    Optional<TemplateVersion> findByTemplateUidAndVersionNumber(@NotNull @Size(max = 11) String templateId, int version);

    @Cacheable(cacheNames = TEMPLATE_UID_VERSION_UID_JPA_CACHE)
    Optional<TemplateVersion> findByTemplateUidAndUid(@NotNull @Size(max = 11) String templateUid, String id);

    // List all versions sorted descending
    Page<TemplateVersion> findAllByTemplateUidOrderByVersionNumberDesc(String templateId, Pageable pageable);

    List<TemplateVersion> findDistinctByTemplateUidInOrderByVersionNumberDesc(Collection<String> uids);

}
