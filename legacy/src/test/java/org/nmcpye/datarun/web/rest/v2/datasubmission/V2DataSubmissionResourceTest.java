package org.nmcpye.datarun.web.rest.v2.datasubmission;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.nmcpye.datarun.datacapture.datasubmission.DataSubmission;
import org.nmcpye.datarun.datacapture.datasubmission.service.DataSubmissionService;
import org.nmcpye.datarun.datacapture.datasubmission.validation.CompositeSubmissionValidator;
import org.nmcpye.datarun.datacapture.datasubmission.validation.SubmissionAccessValidator;
import org.nmcpye.datarun.template.datatemplate.service.TemplateElementService;
import org.nmcpye.datarun.web.rest.v2.datasubmission.service.SubmissionTranslationService;
import org.nmcpye.datarun.sharedkernal.enumeration.FlowStatus;
import org.nmcpye.datarun.web.rest.v2.dto.V2SubmissionDto;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link V2DataSubmissionResource}.
 * Uses Mockito to isolate the controller logic from DB/Spring context.
 */
class V2DataSubmissionResourceTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private DataSubmissionService submissionService;
    private SubmissionTranslationService translationService;
    private CompositeSubmissionValidator compositeValidator;
    private SubmissionAccessValidator submissionAccessValidator;
    private TemplateElementService templateElementService;

    private V2DataSubmissionResource resource;

    @BeforeEach
    void setUp() {
        submissionService = mock(DataSubmissionService.class);
        translationService = mock(SubmissionTranslationService.class);
        compositeValidator = mock(CompositeSubmissionValidator.class);
        submissionAccessValidator = mock(SubmissionAccessValidator.class);
        templateElementService = mock(TemplateElementService.class);

        resource = new V2DataSubmissionResource(
                submissionService,
                translationService,
                objectMapper,
                compositeValidator,
                submissionAccessValidator,
                templateElementService);
    }

    @Test
    @DisplayName("T1: GET by UID → normalizes V1 formData → returns V2SubmissionDto")
    void getByUid_returnsNormalizedDto() {
        // Arrange: V1-shaped submission
        DataSubmission saved = buildV1Submission("sub001", "tmpl01", "ver01");

        when(submissionService.findByUid("sub001")).thenReturn(Optional.of(saved));

        // V1 → canonical normalization
        ObjectNode canonical = objectMapper.createObjectNode();
        ObjectNode values = objectMapper.createObjectNode();
        values.put("visitdate", "2025-09-27");
        values.put("gender", "MALE");
        canonical.set("values", values);
        canonical.set("collections", objectMapper.createObjectNode());

        when(translationService.normalizeV1ToCanonical(
                any(JsonNode.class), eq("tmpl01"), eq("ver01")))
                .thenReturn(canonical);

        // Act
        ResponseEntity<V2SubmissionDto> response = resource.getByUid("sub001");

        // Assert
        assertEquals(200, response.getStatusCode().value());
        V2SubmissionDto dto = response.getBody();
        assertNotNull(dto);
        assertEquals("sub001", dto.getSubmissionUid());
        assertEquals("tmpl01", dto.getTemplateUid());
        assertEquals("ver01", dto.getVersionUid());

        // Check canonical values
        assertNotNull(dto.getValues());
        assertEquals("2025-09-27", dto.getValues().get("visitdate").asText());
        assertEquals("MALE", dto.getValues().get("gender").asText());

        verify(translationService).normalizeV1ToCanonical(any(), eq("tmpl01"), eq("ver01"));
    }

    @Test
    @DisplayName("T2: GET by UID → not found → throws exception")
    void getByUid_notFound() {
        when(submissionService.findByUid("missing")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> resource.getByUid("missing"));
    }

    @Test
    @DisplayName("T3: V2SubmissionDto maps all entity metadata fields")
    void dtoMapsAllMetadata() {
        DataSubmission saved = buildV1Submission("sub002", "tmpl02", "ver02");
        saved.setStatus(FlowStatus.IN_PROGRESS);
        saved.setTeam("team_abc");
        saved.setOrgUnit("ou_xyz");
        saved.setActivity("act_001");
        saved.setAssignment("asgn_001");
        saved.setVersion(3);

        when(submissionService.findByUid("sub002")).thenReturn(Optional.of(saved));

        ObjectNode canonical = objectMapper.createObjectNode();
        canonical.set("values", objectMapper.createObjectNode());
        canonical.set("collections", objectMapper.createObjectNode());
        when(translationService.normalizeV1ToCanonical(any(), any(), any()))
                .thenReturn(canonical);

        ResponseEntity<V2SubmissionDto> response = resource.getByUid("sub002");
        V2SubmissionDto dto = response.getBody();

        assertNotNull(dto);
        assertEquals("IN_PROGRESS", dto.getStatus());
        assertEquals("team_abc", dto.getTeam());
        assertEquals("ou_xyz", dto.getOrgUnit());
        assertEquals("act_001", dto.getActivity());
        assertEquals("asgn_001", dto.getAssignment());
        assertEquals(3, dto.getVersionNumber());
    }

    @Test
    @DisplayName("T4: V2SubmissionDto handles null collections gracefully")
    void dtoHandlesNullCollections() {
        DataSubmission saved = buildV1Submission("sub003", "tmpl03", "ver03");
        when(submissionService.findByUid("sub003")).thenReturn(Optional.of(saved));

        // Canonical with only values, no collections key
        ObjectNode canonical = objectMapper.createObjectNode();
        canonical.set("values", objectMapper.createObjectNode());
        when(translationService.normalizeV1ToCanonical(any(), any(), any()))
                .thenReturn(canonical);

        ResponseEntity<V2SubmissionDto> response = resource.getByUid("sub003");
        V2SubmissionDto dto = response.getBody();

        assertNotNull(dto);
        assertNotNull(dto.getValues());
        assertNull(dto.getCollections());
    }

    private DataSubmission buildV1Submission(String uid, String form, String formVersion) {
        DataSubmission sub = new DataSubmission();
        sub.setUid(uid);
        sub.setForm(form);
        sub.setFormVersion(formVersion);

        // Build V1-shaped formData
        ObjectNode formData = objectMapper.createObjectNode();
        ObjectNode main = objectMapper.createObjectNode();
        main.put("visitdate", "2025-09-27");
        formData.set("main", main);
        ObjectNode patients = objectMapper.createObjectNode();
        patients.put("gender", "MALE");
        formData.set("patients", patients);

        sub.setFormData(formData);
        return sub;
    }
}
