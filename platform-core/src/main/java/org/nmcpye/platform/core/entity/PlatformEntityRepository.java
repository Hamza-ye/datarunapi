package org.nmcpye.platform.core.entity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PlatformEntityRepository extends JpaRepository<PlatformEntity, UUID> {

    Optional<PlatformEntity> findByUid(String uid);

    List<PlatformEntity> findByType_Code(String typeCode);

    boolean existsByUid(String uid);
}
