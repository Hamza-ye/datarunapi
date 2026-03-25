package org.nmcpye.datarun.jpa.orgunit.repository;

import org.nmcpye.datarun.jpa.orgunit.OrgUnit;
import org.nmcpye.datarun.sharedkernal.JpaIdentifiableRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for the {@link OrgUnit} entity.
 *
 * @author Hamza Assada 18/01/2022
 */
@SuppressWarnings("unused")
@Repository
public interface OrgUnitRepository
    extends OrgUnitRepositoryWithBagRelationships,
    JpaIdentifiableRepository<OrgUnit> {
    Boolean existsByCode(String code);

    Optional<OrgUnit> findByCode(String code);

    Page<OrgUnit> findAllByPathIsNull(Pageable pageable);

    @Query(
        value = "select distinct orgUnit from OrgUnit orgUnit " +
            "left join fetch orgUnit.parent " +
            "join orgUnit.assignments fr " +
            "join fr.team.users u " +
            "where u.login = ?#{authentication.name}"
    )
    List<OrgUnit> findAllWithRelation();

    /// ////// new testing
    @Query(value = """
        SELECT DISTINCT ou.*
        FROM org_unit ou
        WHERE ou.uid IN (
            SELECT unnest(string_to_array(acc.path, ','))
            FROM org_unit acc
            WHERE acc.id IN :accessibleIds
        )
        """, nativeQuery = true)
    List<OrgUnit> findWithAncestors(@Param("accessibleIds") List<Long> accessibleIds);

    @Query(value = """
        SELECT ou.*
        FROM org_unit ou
        WHERE EXISTS (
            SELECT 1
            FROM org_unit acc
            WHERE acc.id IN :accessibleIds
            AND ou.path LIKE CONCAT(acc.path, ',%')
        )
        """, nativeQuery = true)
    List<OrgUnit> findWithDescendants(@Param("accessibleIds") List<Long> accessibleIds);

    Page<OrgUnit> findAllByParentIsNullAndPathIsNull(Pageable pageable);

    @Query("SELECT ou FROM OrgUnit ou WHERE ou.path IS NULL AND ou.parent IS NOT NULL AND ou.parent.path IS NOT NULL")
    Page<OrgUnit> findNonRootWithNullPathAndParentPathNotNull(Pageable pageable);

    Page<OrgUnit> findAllByParentIsNull(Pageable pageable);

    @Query("SELECT ou.id FROM OrgUnit ou WHERE ou.parent.id IN :parentIds")
    List<Long> findChildIdsByParentIds(@Param("parentIds") List<Long> parentIds);

    @Query("SELECT ou FROM OrgUnit ou JOIN FETCH ou.parent WHERE ou.id IN :childIds")
    List<OrgUnit> findAllByIdsWithParent(@Param("childIds") List<Long> childIds);
    /////
//    @Query(
//        value = "select distinct orgUnit from OrgUnit orgUnit " +
//            "left join fetch orgUnit.parent " +
//            "join orgUnit.assignments assignments " +
//            "join assignments.team.users user " +
//            "where user.login = ?#{authentication.name}",
//        countQuery = "select count(distinct orgUnit) from OrgUnit orgUnit " +
//            "join orgUnit.assignments assignments " +
//            "join assignments.team.users user " +
//            "where user.login = ?#{authentication.name}"
//    )
//    Page<OrgUnit> findAllWithEagerRelation(Pageable pageable);

//    @Query(
//        value = "select distinct orgUnit from OrgUnit orgUnit " +
//            "left join fetch orgUnit.parent " /*+
//            "join Assignment assignment ON assignment.orgUnit = orgUnit " +
//            "where assignment.team.userInfo.login = ?#{authentication.name}"*/
//    )
//    List<OrgUnit> findAllWithEagerRelation();
//
//    @Query(
//        value = "select distinct orgUnit from OrgUnit orgUnit " +
//            "left join fetch orgUnit.parent " /*+
//            "join Assignment assignment ON assignment.orgUnit = orgUnit " +
//            "where orgUnit.uid =:uid and assignment.team.userInfo.login = ?#{authentication.name}"*/
//    )
//    Optional<OrgUnit> findOneByUidEager(@Param("uid") String uid);
}
