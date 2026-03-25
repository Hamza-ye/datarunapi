package org.nmcpye.datarun.analytics.etl.admin;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.nmcpye.datarun.datacapture.datasubmission.DataSubmission;
import org.nmcpye.datarun.datacapture.datasubmission.repository.DataSubmissionRepository;
import org.nmcpye.datarun.outbox.repository.OutboxWritePort;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BackfillServiceImpl implements BackfillService {
    private final DataSubmissionRepository submissionRepo;
    private final OutboxWritePort outboxRepo;
    private final ObjectMapper objectMapper;

    @Override
    public int enqueueBySubmissionIds(List<String> submissionIds) {
        if (submissionIds == null || submissionIds.isEmpty())
            return 0;
        List<DataSubmission> submissions = submissionRepo.findAllById(submissionIds);
        return enqueueSubmissions(submissions);
    }

    @Override
    public int enqueueBySerialRange(Long fromSerial, Long toSerial) {
        if (fromSerial > toSerial)
            return 0;
        List<DataSubmission> submissions = submissionRepo.findBySerialNumberBetween(fromSerial, toSerial);
        return enqueueSubmissions(submissions);
    }

    private int enqueueSubmissions(List<DataSubmission> submissions) {
        if (submissions == null || submissions.isEmpty())
            return 0;

        List<OutboxWritePort.OutboxInsert> inserts = new ArrayList<>(submissions.size());
        for (DataSubmission s : submissions) {
            if (s == null)
                continue;
            String submissionId = s.getId();
            if (submissionId == null)
                continue;

            String payload;
            try {
                payload = objectMapper.writeValueAsString(s.getFormData());
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }

            String submissionUid = s.getUid();
            String templateVersionUid = s.getFormVersion();

            OutboxWritePort.OutboxInsert insert = new OutboxWritePort.OutboxInsert(
                    submissionId,
                    submissionUid,
                    templateVersionUid,
                    payload,
                    Instant.now(),
                    s.getSerialNumber(),
                    s.getId(), // correlationId
                    s.getFinishedEntryTime() != null ? s.getFinishedEntryTime() : Instant.now() // occurredAt
            );
            inserts.add(insert);
        }

        if (inserts.isEmpty())
            return 0;
        return outboxRepo.insertBackfillIfNotExists(inserts);
    }
}
