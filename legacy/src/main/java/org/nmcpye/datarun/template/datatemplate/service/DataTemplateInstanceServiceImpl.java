package org.nmcpye.datarun.template.datatemplate.service;

import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.nmcpye.datarun.template.datatemplate.DataTemplate;
import org.nmcpye.datarun.template.datatemplate.TemplateVersion;
import org.nmcpye.datarun.template.datatemplate.dto.DataTemplateInstanceDto;
import org.nmcpye.datarun.template.datatemplate.mapper.DataTemplateMapper;
import org.nmcpye.datarun.template.datatemplate.mapper.FormJpaTemplateVersionMapper;
import org.nmcpye.datarun.template.datatemplate.repository.DataTemplateRepository;
import org.nmcpye.datarun.template.datatemplate.repository.TemplateVersionRepository;
import org.nmcpye.datarun.template.datatemplategenerator.TemplateElementGeneratorService;
import org.nmcpye.datarun.iam.user.repository.UserRepository;
import org.nmcpye.datarun.sharedkernal.apiquery.QueryRequest;
import org.nmcpye.datarun.sharedkernal.exceptions.IllegalQueryException;
import org.nmcpye.datarun.sharedkernal.feedback.ErrorCode;
import org.nmcpye.datarun.sharedkernal.uidgenerate.CodeGenerator;
import org.nmcpye.datarun.template.datatemplate.dto.FormTemplateVersionDto;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/// Service Implementation for managing [DataTemplate].
@Service
@Primary
@Transactional
@Slf4j
@RequiredArgsConstructor
public class DataTemplateInstanceServiceImpl
    implements DataTemplateInstanceService {
    private final DataTemplateService dataTemplateService;
    private final DataTemplateRepository dataTemplateRepository;
    private final FormJpaTemplateVersionMapper jpaVersionMapper;

    private final TemplateVersionRepository jpaTemplateVersionRepository;
    private final DataTemplateMapper dataTemplateMapper;
    private final TemplateElementGeneratorService elementGeneratorService;
    /**
     * Create a brand-new TemplateVersion and atomically flip the DataTemplate latest pointer to it.
     * Uses a PESSIMISTIC_WRITE lock on the DataTemplate to avoid concurrent versionNumber races.
     */
    @Transactional
    public DataTemplateInstanceDto saveNewVersion(DataTemplateInstanceDto dto) {
        log.debug("Create new template version for template uid={}", dto.getUid());

        // Build domain object from DTO (this does not touch DB yet)
        DataTemplate incomingTemplate = dataTemplateMapper.fromInstanceDto(dto);

        // Try to lock existing DataTemplate for write (or create new)
        DataTemplate template = dataTemplateRepository
            .findByUidForWrite(incomingTemplate.getUid())
            .map(existing -> {
                // existing: bump version number in memory; we'll persist later
                existing.setVersionNumber(existing.getVersionNumber() + 1);
                existing.setVersionUid(CodeGenerator.generateUid());
                existing.setLabel(incomingTemplate.getLabel());
                existing.setName(incomingTemplate.getName());
                existing.setDescription(incomingTemplate.getDescription());
                existing.setCode(incomingTemplate.getCode());
                return existing;
            })
            .orElseGet(() -> {
                // new template: initialize versionNumber = 1 and persist to obtain DB identity
                // so that TemplateVersion can reference the persisted DataTemplate (FK)
                incomingTemplate.setVersionNumber(1);
                incomingTemplate.setVersionUid(CodeGenerator.generateUid());
                return dataTemplateRepository.persist(incomingTemplate);
            });

        final int newVersionNumber = template.getVersionNumber();
        final String newVersionUid = template.getVersionUid();
        log.debug("Using versionNumber, versionUid={}:{} for template uid={}", newVersionNumber, newVersionUid, template.getUid());

        // Build TemplateVersion entity (immutable version snapshot)
        TemplateVersion version = jpaVersionMapper.fromInstanceDto(dto);
        // override fields that must come from DB logic
        version.setVersionNumber(newVersionNumber);
        version.setUid(newVersionUid);
        version.setTemplateUid(template.getUid());
        version.setDataTemplate(template); // set FK

        // Persist the TemplateVersion
        TemplateVersion persistedVersion = jpaTemplateVersionRepository.persist(version);
        log.debug("Persisted TemplateVersion uid={} for template uid={}", persistedVersion.getUid(), template.getUid());

        // Update the DataTemplate latest-pointer fields and persist the change
        // versionNumber is already set earlier (bumped or 1).
//        template.setVersionUid(persistedVersion.getUid());
        DataTemplate mergedTemplate = dataTemplateRepository.merge(template);

        log.debug("Updated DataTemplate uid={} latest versionUid={} versionNumber={}",
            mergedTemplate.getUid(), mergedTemplate.getVersionUid(), mergedTemplate.getVersionNumber());

        log.info("Successfully created new TemplateVersion uid={} for DataTemplate uid={}",
            persistedVersion.getUid(), mergedTemplate.getUid());

        // both saved, Build and return DTO combining template + version snapshot
        return dataTemplateMapper.toInstanceDto(
            dataTemplateMapper.toDto(mergedTemplate),
            jpaVersionMapper.toDto(persistedVersion)
        );
    }

    @Override
    public List<DataTemplateInstanceDto> findAllByUidIn(Collection<String> uids) {
        final var masters = dataTemplateRepository.findAllByUidIn(uids);
        // batch-load versions
        List<String> ids = masters.stream()
            .map(DataTemplate::getVersionUid)
            .toList();

        Map<String, FormTemplateVersionDto> versions = jpaTemplateVersionRepository.findAllByUidIn(ids).stream()
            .map(jpaVersionMapper::toDto)
            .collect(Collectors.toMap(FormTemplateVersionDto::getTemplateUid, Function.identity()));

        return masters.stream().map(m ->
            dataTemplateMapper.toInstanceDto(dataTemplateMapper.toDto(m),
                Optional.ofNullable(versions.get(m.getUid())).orElseThrow(
                    () -> new IllegalQueryException(ErrorCode.E1120, m.getUid())
                ))).toList();
    }

    @Override
    public List<DataTemplateInstanceDto> findAllByLastModifiedDateAfter(Instant date) {
        final var masters = dataTemplateRepository.findAllByLastModifiedDateAfter(date);
        // batch-load versions
        List<String> ids = masters.stream()
            .map(DataTemplate::getVersionUid)
            .toList();

        Map<String, FormTemplateVersionDto> versions = jpaTemplateVersionRepository.findAllByUidIn(ids).stream()
            .map(jpaVersionMapper::toDto)
            .collect(Collectors.toMap(FormTemplateVersionDto::getTemplateUid, Function.identity()));

        return masters.stream().map(m ->
            dataTemplateMapper.toInstanceDto(dataTemplateMapper.toDto(m),
                Optional.ofNullable(versions.get(m.getUid())).orElseThrow(
                    () -> new IllegalQueryException(ErrorCode.E1120, m.getUid())
                ))).toList();
    }

    @Override
    public List<DataTemplateInstanceDto> findAll() {
        final var masters = Lists.newArrayList(dataTemplateRepository.findAll());
        // batch-load versions
        List<String> ids = masters.stream()
            .map(DataTemplate::getVersionUid)
            .toList();

        Map<String, FormTemplateVersionDto> versions = jpaTemplateVersionRepository.findAllByUidIn(ids).stream()
            .map(jpaVersionMapper::toDto)
            .collect(Collectors.toMap(FormTemplateVersionDto::getTemplateUid, Function.identity()));

        return masters.stream().map(m ->
            dataTemplateMapper.toInstanceDto(dataTemplateMapper.toDto(m),
                Optional.ofNullable(versions.get(m.getUid())).orElseThrow(
                    () -> new IllegalQueryException(ErrorCode.E1120, m.getUid())
                ))).toList();
    }

    @Override
    public Page<DataTemplateInstanceDto> findAllByUidIn(Collection<String> uids, Pageable pageable) {
        final var masters = dataTemplateRepository.findAllByUidIn(uids, pageable);
        // batch-load versions
        List<String> ids = masters.stream()
            .map(DataTemplate::getVersionUid)
            .toList();

        Map<String, FormTemplateVersionDto> versions = jpaTemplateVersionRepository.findAllByUidIn(ids).stream()
            .map(jpaVersionMapper::toDto)
            .collect(Collectors.toMap(FormTemplateVersionDto::getTemplateUid, Function.identity()));

        return masters.map(m -> dataTemplateMapper.toInstanceDto(dataTemplateMapper.toDto(m),
            Optional.ofNullable(versions.get(m.getUid())).orElseThrow(
                () -> new IllegalQueryException(ErrorCode.E1120, m.getUid())
            )));
    }

    @Override
    public Page<DataTemplateInstanceDto> findAllByUser(QueryRequest queryRequest, String jsonQueryBody) {
        // load only lightweight masters
        Page<DataTemplate> masters = dataTemplateService.findAllByUser(queryRequest, jsonQueryBody);
        // batch-load versions
        List<String> ids = masters.stream()
            .map(DataTemplate::getVersionUid)
            .toList();

        Map<String, FormTemplateVersionDto> versions = jpaTemplateVersionRepository.findAllByUidIn(ids).stream()
            .map(jpaVersionMapper::toDto)
            .collect(Collectors.toMap(FormTemplateVersionDto::getTemplateUid, Function.identity()));

        return masters.map(m -> dataTemplateMapper.toInstanceDto(dataTemplateMapper.toDto(m),
            Optional.ofNullable(versions.get(m.getUid())).orElseThrow(
                () -> new IllegalQueryException(ErrorCode.E1120, m.getUid())
            )));
    }

//    @Transactional
//    @Override
//    public void migrateDataFormTemplateVersion(DataFormTemplate formTemplate) {
//        if (!dataTemplateRepository.existsByUid(formTemplate.getUid())) {
//            final var formTemplateVersion = formTemplate.getVersion();
//            final var templateUid = formTemplate.getUid();
//
//            final DataTemplateInstanceDto dataTemplateInstanceDto = dataFormTemplateMapper.toDto(formTemplate);
//            final DataTemplate template = dataTemplateMapper.fromInstanceDto(dataTemplateInstanceDto);
//
//            final var newVersionUid = CodeGenerator.generateUid();
//
//            dataTemplateService.save(template.versionNumber(formTemplateVersion)
//                // temporary for migrating old DataFormTemplate
//                .uid(templateUid)
//                .versionNumber(formTemplateVersion)
//                .versionUid(newVersionUid));
//
//            templateVersionRepository.save(versionMapper
//                .fromInstanceDto(dataTemplateInstanceDto)
//                .uid(newVersionUid)
//                .version(formTemplateVersion)
//                .templateUid(templateUid));
//        }
//    }

    @CacheEvict(cacheNames = {
        UserRepository.USER_TEAM_FORM_ACCESS_CACHE,
        UserRepository.USER_ACTIVITY_IDS_CACHE,
        UserRepository.USER_TEAM_IDS_CACHE,
        DataTemplateRepository.TEMPLATE_BY_UID_CACHE,
        TemplateElementService.TEMPLATE_MAP_CACHE,
        TemplateVersionRepository.TEMPLATE_UID_VERSION_UID_JPA_CACHE,
        TemplateVersionRepository.TEMPLATE_UID_VERSION_NO_JPA_CACHE,
        TemplateVersionRepository.TEMPLATE_UID_LATEST_VERSION_JPA_CACHE,
    })
    @Override
    public DataTemplateInstanceDto save(DataTemplateInstanceDto dataTemplateInstanceDto) {
        final var saved = saveNewVersion(dataTemplateInstanceDto);
        elementGeneratorService.generate(saved.getUid(), saved.getVersionUid());
        return saved;
    }

    @Override
    public boolean existsByUid(String uid) {
        return findLatestByTemplate(uid).isPresent();
    }

    @Override
    public Optional<DataTemplateInstanceDto> findByUid(String uid) {
        return findLatestByTemplate(uid);
    }

    @CacheEvict(cacheNames = {
        UserRepository.USER_TEAM_FORM_ACCESS_CACHE,
        UserRepository.USER_ACTIVITY_IDS_CACHE,
        UserRepository.USER_TEAM_IDS_CACHE,
        DataTemplateRepository.TEMPLATE_BY_UID_CACHE,
    })
    @Override
    public void deleteByUid(String uid) {
        dataTemplateService.deleteByUid(uid);
    }

    @CacheEvict(cacheNames = {
        UserRepository.USER_TEAM_FORM_ACCESS_CACHE,
        UserRepository.USER_ACTIVITY_IDS_CACHE,
        UserRepository.USER_TEAM_IDS_CACHE,
        DataTemplateRepository.TEMPLATE_BY_UID_CACHE,
    })
    @Override
    public DataTemplateInstanceDto update(DataTemplateInstanceDto dataTemplateInstanceDto) {
        dataTemplateRepository.findByUid(dataTemplateInstanceDto.getUid()).orElseThrow(() ->
            new IllegalQueryException(ErrorCode.E1113, dataTemplateInstanceDto.getUid()));
        final var updated = save(dataTemplateInstanceDto);
        elementGeneratorService.generate(updated.getUid(), updated.getVersionUid());
        return updated;
    }

    @Override
    public Optional<DataTemplateInstanceDto> findLatestByTemplate(String templateUid) {
        final var template = dataTemplateService.findByUid(templateUid).map(dataTemplateMapper::toDto);
        final var versionOp = jpaTemplateVersionRepository.findTopByTemplateUidOrderByVersionNumberDesc(templateUid);
        final var version = versionOp
            .map(jpaVersionMapper::toDto);
        if (template.isEmpty() || version.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(dataTemplateMapper.toInstanceDto(template.get(), version.get()));
    }

    @Override
    public Optional<DataTemplateInstanceDto> findByTemplateAndVersionUid(String templateUid,
                                                                         String versionUid) {
        final var template = dataTemplateService.findByUid(templateUid).map(dataTemplateMapper::toDto);
        final var version = jpaTemplateVersionRepository.findByTemplateUidAndUid(templateUid, versionUid)
            .map(jpaVersionMapper::toDto);

        if (template.isEmpty() || version.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(dataTemplateMapper.toInstanceDto(template.get(), version.get()));
    }

    @Override
    public Optional<DataTemplateInstanceDto> findByTemplateAndVersionNo(String templateUid, Integer versionNumber) {
        final var template = dataTemplateService.findByUid(templateUid).map(dataTemplateMapper::toDto);
        final var version =
            jpaTemplateVersionRepository.findByTemplateUidAndVersionNumber(templateUid, versionNumber)
                .map(jpaVersionMapper::toDto);

        if (template.isEmpty() || version.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(dataTemplateMapper.toInstanceDto(template.get(), version.get()));
    }

    @Override
    public void delete(DataTemplateInstanceDto object) {
        deleteByUid(object.getUid());
    }
}
