package org.nmcpye.datarun.analytics.etl.repository;

import io.hypersistence.utils.spring.repository.BaseJpaRepository;
import org.nmcpye.datarun.analytics.etl.entity.EtlRun;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Standard Spring Data JPA repository for etl_runs.
 * Future: add custom queries for paging runs, filtering by status, and retention cleanup.
 */
@Repository
public interface EtlRunRepository extends BaseJpaRepository<EtlRun, UUID> {
    // custom queries (if needed) will be added here
}
