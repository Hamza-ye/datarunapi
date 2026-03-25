package org.nmcpye.datarun.template.datatemplate.repository;

import jakarta.persistence.LockModeType;

import org.nmcpye.datarun.template.datatemplate.DataTemplate;
import org.nmcpye.datarun.sharedkernal.JpaIdentifiableRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/// Spring Data jpa repository for the DataTemplate entity.
@Repository
public interface DataTemplateRepository
    extends JpaIdentifiableRepository<DataTemplate> {
    String TEMPLATE_BY_UID_CACHE = "templateByUidCache";

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT f FROM DataTemplate f WHERE f.uid = :uid")
    Optional<DataTemplate> findByUidForWrite(@Param("uid") String uid);

    List<DataTemplate> findAllByLastModifiedDateAfter(Instant lastModifiedDateAfter);

    @Cacheable(cacheNames = TEMPLATE_BY_UID_CACHE)
    @Override
    Optional<DataTemplate> findByUid(String uid);

    @Query("SELECT f FROM DataTemplate f WHERE f.uid IN :uids OR f.code IN :uids")
    List<DataTemplate> findAllByCodeOrUidsIn(@Param("uids") Collection<String> uids);


    Optional<DataTemplate> findFirstByCodeOrUidOrId(String code, String uid, String id);

    @Query("SELECT d.uid FROM DataTemplate d")
    List<String> findAllUids();
}
