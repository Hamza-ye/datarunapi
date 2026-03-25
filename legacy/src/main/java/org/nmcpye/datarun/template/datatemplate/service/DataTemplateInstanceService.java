package org.nmcpye.datarun.template.datatemplate.service;

import org.nmcpye.datarun.template.datatemplate.DataTemplate;
import org.nmcpye.datarun.template.datatemplate.dto.DataTemplateInstanceDto;
import org.nmcpye.datarun.sharedkernal.apiquery.QueryRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Service Custom Interface for managing {@link DataTemplate}.
 */
public interface DataTemplateInstanceService {

    boolean existsByUid(String uid);

    Optional<DataTemplateInstanceDto> findByUid(String uid);

    void deleteByUid(String uid);

    Page<DataTemplateInstanceDto> findAllByUidIn(Collection<String> uids, Pageable pageable);
    List<DataTemplateInstanceDto> findAllByUidIn(Collection<String> uids);
    List<DataTemplateInstanceDto> findAllByLastModifiedDateAfter(Instant date);
    List<DataTemplateInstanceDto> findAll();

    Page<DataTemplateInstanceDto> findAllByUser(QueryRequest queryRequest, String jsonQueryBody);

    /**
     * Save an object.
     *
     * @param object the entity to save.
     * @return the persisted entity.
     */
    DataTemplateInstanceDto save(DataTemplateInstanceDto object);

    /**
     * Updates a object.
     *
     * @param object the entity to update.
     * @return the persisted entity.
     */
    DataTemplateInstanceDto update(DataTemplateInstanceDto object);

    /**
     * Delete an object.
     *
     * @param object the entity to delete.
     */
    void delete(DataTemplateInstanceDto object);

    DataTemplateInstanceDto saveNewVersion(DataTemplateInstanceDto dataTemplateInstanceDto);

//    void migrateDataFormTemplateVersionToLegacy(DataFormTemplate formTemplate);

//    void migrateDataFormTemplateVersion(DataFormTemplate formTemplate);

    Optional<DataTemplateInstanceDto> findLatestByTemplate(String templateUid);

    Optional<DataTemplateInstanceDto> findByTemplateAndVersionUid(String templateUid, String versionUid);

    Optional<DataTemplateInstanceDto> findByTemplateAndVersionNo(String templateUid, Integer version);
}
