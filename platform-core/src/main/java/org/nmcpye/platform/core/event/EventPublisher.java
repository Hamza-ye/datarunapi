package org.nmcpye.platform.core.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Creates and persists platform events.
 * Called by controllers on every mutation to ensure traceability.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EventPublisher {

    private final PlatformEventRepository eventRepository;
    private final ObjectMapper objectMapper;

    /**
     * Record an event. This is synchronous and transactional —
     * the event is committed in the same transaction as the mutation.
     */
    @Transactional
    public PlatformEvent publish(String type,
                                 String subjectType,
                                 UUID subjectId,
                                 UUID actorId,
                                 Map<String, Object> payload) {
        PlatformEvent event = new PlatformEvent();
        event.setType(type);
        event.setSubjectType(subjectType);
        event.setSubjectId(subjectId);
        event.setActorId(actorId);
        event.setTimestamp(Instant.now());

        if (payload != null && !payload.isEmpty()) {
            try {
                event.setPayload(objectMapper.writeValueAsString(payload));
            } catch (JsonProcessingException e) {
                log.warn("Failed to serialize event payload for type={}, subjectId={}", type, subjectId, e);
                event.setPayload("{}");
            }
        }

        PlatformEvent saved = eventRepository.save(event);
        log.info("Event published: type={} subjectType={} subjectId={}", type, subjectType, subjectId);
        return saved;
    }
}
