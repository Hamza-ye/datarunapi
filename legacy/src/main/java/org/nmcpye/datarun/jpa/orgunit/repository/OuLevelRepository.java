package org.nmcpye.datarun.jpa.orgunit.repository;

import org.nmcpye.datarun.jpa.orgunit.OuLevel;
import org.nmcpye.datarun.sharedkernal.JpaIdentifiableRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Project entity.
 */
@SuppressWarnings("unused")
@Repository
public interface OuLevelRepository
    extends JpaIdentifiableRepository<OuLevel> {
}
