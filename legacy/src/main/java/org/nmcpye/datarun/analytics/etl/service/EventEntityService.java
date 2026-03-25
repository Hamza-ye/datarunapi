package org.nmcpye.datarun.analytics.etl.service;

import lombok.RequiredArgsConstructor;
import org.nmcpye.datarun.analytics.etl.dto.EventDto;
import org.nmcpye.datarun.analytics.etl.repository.EventEntityJdbcRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EventEntityService {
    private final EventEntityJdbcRepository eventRepo;

    /**
     * Upsert an event row for instanceKey. If eventUid is null we generate one;
     * repository upsert keeps original event_uid on conflict to preserve identity.
     */
    public void upsert(EventDto eventDto) {
        eventRepo.upsertEventEntity(eventDto);
    }

    public Optional<EventDto> findByInstanceKey(String instanceKey) {
        return eventRepo.findByInstanceKey(instanceKey);
    }
}
