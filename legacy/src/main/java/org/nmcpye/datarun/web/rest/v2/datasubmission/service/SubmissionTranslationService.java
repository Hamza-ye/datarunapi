package org.nmcpye.datarun.web.rest.v2.datasubmission.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.nmcpye.datarun.template.datatemplateelement.FieldTemplateElementDto;
import org.nmcpye.datarun.template.datatemplateelement.SectionTemplateElementDto;
import org.nmcpye.datarun.template.datatemplate.TemplateVersion;
import org.nmcpye.datarun.template.datatemplate.repository.TemplateVersionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Facade service that loads template metadata and delegates
 * to {@link SubmissionNormalizer} and {@link SubmissionDenormalizer}.
 * <p>
 * This is the entry point for the ACL translation layer.
 * It does NOT modify any existing REST endpoints — that happens in Phase 2.
 *
 * @author Hamza Assada
 */
@Service
public class SubmissionTranslationService {

    private static final Logger log = LoggerFactory.getLogger(SubmissionTranslationService.class);

    private final TemplateVersionRepository templateVersionRepository;
    private final ObjectMapper objectMapper;

    public SubmissionTranslationService(TemplateVersionRepository templateVersionRepository,
            ObjectMapper objectMapper) {
        this.templateVersionRepository = templateVersionRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Normalize V1 formData into canonical V2 shape.
     *
     * @param v1FormData  the V1 formData blob
     * @param templateUid the template UID (to look up sections)
     * @param versionUid  the template version UID
     * @return canonical JsonNode with "values" and "collections"
     */
    public JsonNode normalizeV1ToCanonical(JsonNode v1FormData,
            String templateUid,
            String versionUid) {
        TemplateVersion version = loadVersion(versionUid);
        return SubmissionNormalizer.normalize(v1FormData, version.getSections(), objectMapper);
    }

    /**
     * Normalize V1 formData into canonical V2 shape using pre-loaded sections.
     * Use this overload when you already have the sections list (avoids extra DB
     * query).
     *
     * @param v1FormData the V1 formData blob
     * @param sections   section definitions from the TemplateVersion
     * @return canonical JsonNode with "values" and "collections"
     */
    public JsonNode normalizeV1ToCanonical(JsonNode v1FormData,
            List<SectionTemplateElementDto> sections) {
        return SubmissionNormalizer.normalize(v1FormData, sections, objectMapper);
    }

    /**
     * Denormalize canonical V2 formData back into V1 shape.
     *
     * @param canonical     the canonical formData blob
     * @param templateUid   the template UID
     * @param versionUid    the template version UID
     * @param submissionUid the submission UID (needed for _parentId/_submissionUid
     *                      restoration)
     * @return V1-shaped formData JsonNode
     */
    public JsonNode denormalizeCanonicalToV1(JsonNode canonical,
            String templateUid,
            String versionUid,
            String submissionUid) {
        TemplateVersion version = loadVersion(versionUid);
        return SubmissionDenormalizer.denormalize(
                canonical,
                version.getSections(),
                version.getFields(),
                submissionUid,
                objectMapper);
    }

    /**
     * Denormalize canonical V2 formData back into V1 shape using pre-loaded
     * metadata.
     *
     * @param canonical     the canonical formData blob
     * @param sections      section definitions from TemplateVersion
     * @param fields        field definitions from TemplateVersion
     * @param submissionUid the submission UID
     * @return V1-shaped formData JsonNode
     */
    public JsonNode denormalizeCanonicalToV1(JsonNode canonical,
            List<SectionTemplateElementDto> sections,
            List<FieldTemplateElementDto> fields,
            String submissionUid) {
        return SubmissionDenormalizer.denormalize(
                canonical, sections, fields, submissionUid, objectMapper);
    }

    /**
     * Load a TemplateVersion by its UID.
     */
    private TemplateVersion loadVersion(String versionUid) {
        Optional<TemplateVersion> versionOpt = templateVersionRepository.findByUid(versionUid);
        return versionOpt
                .orElseThrow(() -> new IllegalArgumentException("TemplateVersion not found for uid: " + versionUid));
    }
}
