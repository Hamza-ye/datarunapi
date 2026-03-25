package org.nmcpye.datarun.analytics.etl.pivot;

import lombok.Data;

import java.time.Instant;

@Data
public class StatusResponse {
    private String templateUid;
    private String status;
    private Instant buildStartedAt;
    private Instant buildFinishedAt;
    private Integer durationSecs;
    private String notes;
}
