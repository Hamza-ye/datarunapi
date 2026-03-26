package org.nmcpye.platform.core.entity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface EntityTypeRepository extends JpaRepository<EntityType, UUID> {

    Optional<EntityType> findByCode(String code);

    boolean existsByCode(String code);
}
