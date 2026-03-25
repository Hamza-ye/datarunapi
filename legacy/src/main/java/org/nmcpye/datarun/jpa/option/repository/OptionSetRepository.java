package org.nmcpye.datarun.jpa.option.repository;

import jakarta.transaction.Transactional;

import org.nmcpye.datarun.jpa.option.OptionSet;
import org.nmcpye.datarun.sharedkernal.JpaIdentifiableRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * @author Hamza Assada
 * @since 10/09/2024
 */
@Repository
public interface OptionSetRepository
    extends JpaIdentifiableRepository<OptionSet> {
    @Query("SELECT os FROM OptionSet os " +
        "WHERE (:includeDeleted = true OR os.deletedAt IS NULL) " +
        "  AND (:code IS NULL OR os.code = :code)")
    Optional<OptionSet> findByCodeIncludeDeleted(@Param("code") String code,
                                                 @Param("includeDeleted") boolean includeDeleted);

    @Query("SELECT os FROM OptionSet os " +
        "WHERE (:includeDeleted = true OR os.deletedAt IS NULL) " +
        "  AND (os.id = :id OR os.uid = :id)")
    Optional<OptionSet> findByUidIncludeDeleted(@Param("id") String id,
                                                 @Param("includeDeleted") boolean includeDeleted);

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("UPDATE OptionSet os SET os.deletedAt = CURRENT_TIMESTAMP WHERE (os.id = :id OR os.uid = :id) AND os.deletedAt IS NULL")
    int softDeleteOptionSetById(@Param("id") String id);

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("UPDATE OptionSet os SET os.deletedAt = NULL WHERE (os.id = :id OR os.uid = :id) AND os.deletedAt IS NOT NULL")
    int undeleteOptionSetById(@Param("id") String id);
}
