package org.nmcpye.datarun.jpa.orgunit.repository;

import org.nmcpye.datarun.jpa.orgunit.OrgUnitGroup;
import org.nmcpye.datarun.sharedkernal.JpaIdentifiableRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA repository for the Project entity.
 */
@SuppressWarnings("unused")
@Repository
public interface OrgUnitGroupRepository
    extends JpaIdentifiableRepository<OrgUnitGroup> {

    Optional<OrgUnitGroup> findByCode(String code);
}
