package org.nmcpye.datarun.analytics.etl.service;

import org.nmcpye.datarun.outbox.dto.OutboxDto;

import java.util.UUID;

public interface EventProcessorService {
    void processEvent(UUID etlRunId, OutboxDto outbox, UUID ingestId) throws Exception;
}
