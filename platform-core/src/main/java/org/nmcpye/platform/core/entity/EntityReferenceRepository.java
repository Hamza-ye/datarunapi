package org.nmcpye.platform.core.entity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface EntityReferenceRepository extends JpaRepository<EntityReference, UUID> {

    List<EntityReference> findBySourceTypeAndSourceId(String sourceType, UUID sourceId);

    List<EntityReference> findByTargetEntity_Id(UUID targetEntityId);
}
