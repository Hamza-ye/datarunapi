package org.nmcpye.datarun.datacapture.datasubmission.events;
import lombok.Getter;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * @author Hamza Assada
 * @since 14/08/2025
 */
@Getter
public class SubmissionSavedEvent implements Serializable {
    private final String submissionId;   // DataSubmission.id (ULID)
    private final EventChangeType changeType;
    private final Long submissionVersion;
    private final Instant occurredAt;

    public SubmissionSavedEvent(String submissionId,
                                EventChangeType changeType,
                                Long submissionVersion) {
        this.submissionId = submissionId;
        this.changeType = changeType;
        this.submissionVersion = submissionVersion;
        this.occurredAt = Instant.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SubmissionSavedEvent)) return false;
        SubmissionSavedEvent that = (SubmissionSavedEvent) o;
        return Objects.equals(submissionId, that.submissionId)
            && changeType == that.changeType
            && Objects.equals(submissionVersion, that.submissionVersion);
    }

    @Override
    public int hashCode() {
        return Objects.hash(submissionId, changeType, submissionVersion);
    }
}
