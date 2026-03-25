package org.nmcpye.datarun.jpa.errorevent.repository;


import io.hypersistence.utils.spring.repository.BaseJpaRepository;
import org.nmcpye.datarun.jpa.errorevent.ErrorEvent;
import org.springframework.stereotype.Repository;

@Repository
public interface ErrorEventRepository extends BaseJpaRepository<ErrorEvent, Long> {
}

