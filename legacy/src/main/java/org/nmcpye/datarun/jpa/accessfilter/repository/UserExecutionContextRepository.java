package org.nmcpye.datarun.jpa.accessfilter.repository;

import io.hypersistence.utils.spring.repository.BaseJpaRepository;

import org.nmcpye.datarun.jpa.accessfilter.entity.UserExecutionContext;
import org.nmcpye.datarun.sharedkernal.enumeration.AccessLevel;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for the UserExecutionContext entity.
 */
@Repository
public interface UserExecutionContextRepository extends BaseJpaRepository<UserExecutionContext, Long> {

        void deleteByUserUid(String userUid);

        List<UserExecutionContext> findByUserUid(String userUid);

        List<UserExecutionContext> findByUserUidAndEntityType(String userUid, String entityType);

        Optional<UserExecutionContext> findByUserUidAndEntityTypeAndEntityUid(String userUid, String entityType,
                        String entityUid);

        void deleteByUserUidAndEntityTypeAndEntityUid(String userUid, String entityType, String entityUid);

        void deleteByUserUidAndEntityType(String userUid, String entityType);

        long countByUserUidAndEntityTypeAndResolvedPermissionIn(String userUid, String entityType,
                        List<AccessLevel> permissions);
}
