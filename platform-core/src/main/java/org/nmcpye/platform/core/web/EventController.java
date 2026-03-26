package org.nmcpye.platform.core.web;

import lombok.RequiredArgsConstructor;
import org.nmcpye.platform.core.event.PlatformEvent;
import org.nmcpye.platform.core.event.PlatformEventRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {

    private final PlatformEventRepository eventRepository;

    @GetMapping
    public List<PlatformEvent> query(
            @RequestParam(required = false) UUID subjectId,
            @RequestParam(required = false) String subjectType,
            @RequestParam(required = false) UUID correlationId) {

        if (correlationId != null) {
            return eventRepository.findByCorrelationIdOrderByTimestampAsc(correlationId);
        }
        if (subjectId != null && subjectType != null) {
            return eventRepository.findBySubjectTypeAndSubjectIdOrderByTimestampAsc(subjectType, subjectId);
        }
        if (subjectId != null) {
            return eventRepository.findBySubjectIdOrderByTimestampAsc(subjectId);
        }

        // Return empty if no filter provided — avoid full table scan
        return List.of();
    }
}
