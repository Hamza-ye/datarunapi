package org.nmcpye.platform.core.event;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PlatformEventRepository extends JpaRepository<PlatformEvent, UUID> {

    List<PlatformEvent> findBySubjectIdOrderByTimestampAsc(UUID subjectId);

    List<PlatformEvent> findBySubjectTypeAndSubjectIdOrderByTimestampAsc(String subjectType, UUID subjectId);

    List<PlatformEvent> findByCorrelationIdOrderByTimestampAsc(UUID correlationId);
}
