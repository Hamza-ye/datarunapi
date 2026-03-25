package org.nmcpye.datarun.analytics.etl.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.nmcpye.datarun.analytics.etl.dto.EventRow;
import org.nmcpye.datarun.analytics.etl.dto.FlattenSubmission;
import org.nmcpye.datarun.analytics.etl.dto.TallCanonicalValue;
import org.nmcpye.datarun.analytics.etl.model.TemplateContext;
import org.nmcpye.datarun.template.datatemplate.CanonicalElement;
import org.nmcpye.datarun.template.datatemplate.SemanticType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransformServiceV2 {

    private final TransformServiceRobust baseTransform;
    private final RefTypeValueResolutionService refTypeValueResolutionService;

    @Transactional(readOnly = true)
    public FlattenSubmission transform(JsonNode root,
                                       boolean flattenPrimitiveArrays,
                                       String submissionUid,
                                       TemplateContext templateContext) {

        log.debug("TransformV2: submissionUid={}, payloadNull={}",
            submissionUid, root == null);

        List<TallCanonicalValue> rows = baseTransform.transform(
            root,
            templateContext.allCanonicalElementsMap().values().stream().toList(),
            flattenPrimitiveArrays
        );

        if (rows == null || rows.isEmpty()) {
            return null;
        }

        var flattenSubmissionBuilder = FlattenSubmission.builder();

        Set<String> allInstanceKeys = new HashSet<>();
        Map<String, String> instanceRepeatCeMap = new HashMap<>();
        Map<String, String> parentForInstance = new HashMap<>();

        for (TallCanonicalValue r : rows) {
            String instanceKey = r.getRepeatInstanceId() != null ? r.getRepeatInstanceId() : submissionUid;
            allInstanceKeys.add(instanceKey);

            String explicitParent = r.getParentInstanceId(); // direct call, assumed present on TallCanonicalRow
            if (explicitParent != null) parentForInstance.put(instanceKey, explicitParent);

            String ceId = r.getCanonicalElementId();
            CanonicalElement ceMeta = templateContext.allCanonicalElementsMap().get(ceId);
            SemanticType semanticType = ceMeta == null ? null : ceMeta.getSemanticType();
            String optionSetUid = ceMeta == null ? null : ceMeta.getOptionSetUid();

            if (semanticType != null && semanticType.isRepeat()) {
                instanceRepeatCeMap.putIfAbsent(instanceKey, ceId);
            }

            if (semanticType != null && semanticType.isRef()) {
                String token = r.getValueText();
                String refType = semanticType.name();

                String resolvedUid = refTypeValueResolutionService.resolve(token, refType, optionSetUid);

                r.setValueRefUid(resolvedUid);
                r.setValueRefType(refType);

                if (token == null && r.getValueJson() != null) {
                    r.setValueText(r.getValueJson());
                }
            }

            flattenSubmissionBuilder.tallCanonicalRow(r);
        }


        for (String instanceKey : allInstanceKeys) {
            boolean isRoot = instanceKey.equals(submissionUid);
            String parentInstance = parentForInstance.get(instanceKey);
            String parentEventId = isRoot ? null : (parentInstance != null ? parentInstance :
                submissionUid);

            String eventType = isRoot ? "root" : "repeat";
            String eventCeId = isRoot ? null : instanceRepeatCeMap.get(instanceKey);

            flattenSubmissionBuilder.eventRow(EventRow.builder()
                .eventId(instanceKey)
                .eventType(eventType)
                .parentEventId(parentEventId)
                .eventCeId(eventCeId)
                .build());
        }

        return flattenSubmissionBuilder.build();
    }
}

