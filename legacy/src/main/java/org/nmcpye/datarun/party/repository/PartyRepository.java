package org.nmcpye.datarun.party.repository;

import io.hypersistence.utils.spring.repository.BaseJpaRepository;
import org.nmcpye.datarun.party.entities.Party;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PartyRepository extends BaseJpaRepository<Party, UUID> {
    List<Party> findAll();

    Optional<Party> findBySourceId(String sourceId);

    Optional<Party> findByUid(String uid);
}
