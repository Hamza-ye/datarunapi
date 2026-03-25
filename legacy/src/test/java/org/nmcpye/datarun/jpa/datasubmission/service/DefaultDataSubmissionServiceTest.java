package org.nmcpye.datarun.jpa.datasubmission.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.nmcpye.datarun.datacapture.datasubmission.service.DefaultDataSubmissionService;
import org.nmcpye.datarun.jpa.accessfilter.UserAccessService;
import org.nmcpye.datarun.datacapture.datasubmission.DataSubmission;
import org.nmcpye.datarun.datacapture.datasubmission.repository.DataSubmissionRepository;
import org.nmcpye.datarun.outbox.repository.OutboxWritePort;
import org.nmcpye.datarun.iam.security.CurrentUserDetails;
import org.nmcpye.datarun.sharedkernal.EntitySaveSummaryVM;
import org.springframework.cache.CacheManager;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class DefaultDataSubmissionServiceTest {

    private DefaultDataSubmissionService service;
    private DataSubmissionRepository repository;
    private CacheManager cacheManager;
    private UserAccessService userAccessService;
    private ApplicationEventPublisher eventPublisher;
    private ObjectMapper objectMapper;
    private OutboxWritePort outboxRepo;

    @BeforeEach
    void setUp() {
        repository = mock(DataSubmissionRepository.class);
        cacheManager = mock(CacheManager.class);
        userAccessService = mock(UserAccessService.class);
        eventPublisher = mock(ApplicationEventPublisher.class);
        objectMapper = new ObjectMapper(); // Use real mapper for testing the JSON Fat Payload
        outboxRepo = mock(OutboxWritePort.class);

        service = new DefaultDataSubmissionService(
                repository, cacheManager, userAccessService, eventPublisher, objectMapper, outboxRepo);
    }

    @Test
    void shouldGenerateFatEventPayloadWhenSavingSubmission() throws JsonProcessingException {
        // Arrange
        DataSubmission submission = new DataSubmission();
        submission.setId("SUB-DB-UUID-001");
        submission.setUid("SUB-UID-111");
        submission.setForm("FORM-UID-222");
        submission.setOrgUnit("ORG-UNIT-UID-333");
        submission.setTeam("TEAM-UID-444");
        submission.setSerialNumber(1001L);
        submission.setFinishedEntryTime(Instant.parse("2026-03-08T05:00:00Z"));

        // Use a mock node to represent the form data
        JsonNode mockDataNode = objectMapper.createObjectNode().put("question_1", "Yes");
        submission.setFormData(mockDataNode);

        CurrentUserDetails user = mock(CurrentUserDetails.class);
        EntitySaveSummaryVM summary = new EntitySaveSummaryVM();

        // Stub repository to return isNew = true (empty list) and return the saved
        // entity
        when(repository.findAllByUidIn(any())).thenReturn(List.of());
        when(repository.persistAllAndFlush(any())).thenReturn(List.of(submission));

        // Act
        service.upsertAll(List.of(submission), user, summary);

        // Assert
        ArgumentCaptor<List<OutboxWritePort.OutboxInsert>> captor = ArgumentCaptor.forClass(List.class);
        verify(outboxRepo).insertByEventType(captor.capture(), eq("SAVE"));

        List<OutboxWritePort.OutboxInsert> inserts = captor.getValue();
        assertThat(inserts).hasSize(1);

        OutboxWritePort.OutboxInsert insert = inserts.get(0);

        // Assert bi-temporal & correlation properties on the Database binding
        assertThat(insert.correlationId).isEqualTo("SUB-DB-UUID-001");
        assertThat(insert.occurredAt).isEqualTo(Instant.parse("2026-03-08T05:00:00Z"));
        assertThat(insert.submissionUid).isEqualTo("SUB-UID-111");

        // Assert the Fat Event Payload JSON Structure
        JsonNode fatEventNode = objectMapper.readTree(insert.payload);
        assertThat(fatEventNode.get("aggregate_id").asText()).isEqualTo("SUB-UID-111");
        assertThat(fatEventNode.get("aggregate_type").asText()).isEqualTo("Submission");
        assertThat(fatEventNode.get("correlation_id").asText()).isEqualTo("SUB-DB-UUID-001");
        assertThat(fatEventNode.get("occurred_at").asText()).isEqualTo("2026-03-08T05:00:00Z");
        assertThat(fatEventNode.get("template_uid").asText()).isEqualTo("FORM-UID-222");
        assertThat(fatEventNode.get("org_unit_uid").asText()).isEqualTo("ORG-UNIT-UID-333");
        assertThat(fatEventNode.get("team_uid").asText()).isEqualTo("TEAM-UID-444");

        // Ensure raw data is nested exactly as expected
        assertThat(fatEventNode.get("data").get("question_1").asText()).isEqualTo("Yes");
    }
}
