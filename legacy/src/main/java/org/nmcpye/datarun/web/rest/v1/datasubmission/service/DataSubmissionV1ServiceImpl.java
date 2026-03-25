package org.nmcpye.datarun.web.rest.v1.datasubmission.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;

import org.nmcpye.datarun.datacapture.datasubmission.DataSubmission;
import org.nmcpye.datarun.datacapture.datasubmission.service.DataSubmissionService;
import org.nmcpye.datarun.datacapture.datasubmission.validation.CompositeSubmissionValidator;
import org.nmcpye.datarun.datacapture.datasubmission.validation.SubmissionAccessValidator;
import org.nmcpye.datarun.datacapture.datasubmission.service.MigrationRepeatIdGenerator;
import org.nmcpye.datarun.template.datatemplate.service.TemplateElementService;
import org.nmcpye.datarun.iam.security.SecurityUtils;
import org.nmcpye.datarun.sharedkernal.EntitySaveSummaryVM;
import org.nmcpye.datarun.sharedkernal.apiquery.QueryRequest;
import org.nmcpye.datarun.datacapture.util.FormSubmissionDataUtil;
import org.nmcpye.datarun.web.common.PagedResponse;
import org.nmcpye.datarun.web.rest.v1.datasubmission.dto.DataSubmissionV1Dto;
import org.nmcpye.datarun.web.rest.v1.datasubmission.mapper.DataSubmissionV1Mapper;
import org.nmcpye.datarun.web.rest.v1.paging.PagingConfigurator;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DataSubmissionV1ServiceImpl implements DataSubmissionV1Service {

    private final DataSubmissionService submissionService;
    private final DataSubmissionV1Mapper v1Mapper;
    private final ObjectMapper objectMapper;
    private final CompositeSubmissionValidator compositeValidator;
    private final SubmissionAccessValidator submissionAccessValidator;
    private final TemplateElementService templateElementService;

    @Override
    public PagedResponse<DataSubmissionV1Dto> getAll(QueryRequest queryRequest) {
        // in v1 only superuser can get submission after created, handled in service
        Page<DataSubmission> page = submissionService.findAllByUser(queryRequest, null);
        Page<DataSubmissionV1Dto> dtoPage = page.map(v1Mapper::toDto);
        String next = PagingConfigurator.createNextPageLink(dtoPage);

        if (queryRequest.isFlatten()) {
            return PagingConfigurator.initPageResponse(dtoPage.map(this::postProcess), next, "dataElements");
        } else {
            return PagingConfigurator.initPageResponse(dtoPage, next, "dataElements");
        }

    }

    @Override
    public Optional<DataSubmissionV1Dto> getById(String id) {
        return submissionService.findByIdOrUid(id)
            .map(v1Mapper::toDto);
    }

    @Override
    public EntitySaveSummaryVM upsertAll(List<DataSubmissionV1Dto> dtoList) {
        List<DataSubmission> entities = v1Mapper.toEntity(dtoList);
        List<DataSubmission> processedEntities = preProcess(entities);

        EntitySaveSummaryVM summary = new EntitySaveSummaryVM();
        submissionService.upsertAll(processedEntities, SecurityUtils.getCurrentUserDetailsOrThrow(), summary);
        return summary;
    }

    private List<DataSubmission> preProcess(List<DataSubmission> payLoadEntities) {
        return payLoadEntities.stream()
            .peek(payLoadEntity -> {
                ObjectNode root = (ObjectNode) (payLoadEntity.getFormData() == null
                    ? objectMapper.createObjectNode()
                    : payLoadEntity.getFormData().deepCopy());
                final var migrationRepeatIdGenerator = new MigrationRepeatIdGenerator(
                    templateElementService.getTemplateElementMap(payLoadEntity.getForm(),
                        payLoadEntity.getFormVersion()));
                int generated = migrationRepeatIdGenerator
                    .generateMissingIdsForMigration(root, payLoadEntity.getUid());
                if (generated > 0) {
                    payLoadEntity.setFormData(root);
                }

                compositeValidator.validateAndEnrich(submissionAccessValidator.validateAccess(payLoadEntity,
                    SecurityUtils.getCurrentUserDetailsOrThrow()));
            }).collect(Collectors.toList());
    }

    private DataSubmissionV1Dto postProcess(DataSubmissionV1Dto submission) {
        Map<String, Object> formData = objectMapper.convertValue(submission.getFormData(), new TypeReference<>() {
        });
        formData = FormSubmissionDataUtil.flatten(formData, false, true);
        submission.setFormData(objectMapper.convertValue(formData, JsonNode.class));
        return submission;
    }
}
