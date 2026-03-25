package org.nmcpye.datarun.party.repository;

import io.hypersistence.utils.spring.repository.BaseJpaRepository;
import org.nmcpye.datarun.party.entities.PartySet;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PartySetRepository extends BaseJpaRepository<PartySet, String> {
    List<PartySet> findAll();

    Optional<PartySet> findByUid(String uid);

    boolean existsByUid(String uid);

    void deleteByUid(String uid);
}
