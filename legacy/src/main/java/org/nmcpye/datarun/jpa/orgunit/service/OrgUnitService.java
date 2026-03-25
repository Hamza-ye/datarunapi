package org.nmcpye.datarun.jpa.orgunit.service;

import org.nmcpye.datarun.jpa.orgunit.OrgUnit;
import org.nmcpye.datarun.sharedkernal.JpaIdentifiableObjectService;

/**
 * Service Interface for managing {@link OrgUnit}.
 *
 * @author Hamza Assada 18/01/2022
 */
public interface OrgUnitService
    extends JpaIdentifiableObjectService<OrgUnit> {

//    Set<OrgUnit> getUserTeamsOrganisationUnits();

//    Set<OrgUnit> getUserManagedTeamsOrganisationUnits();

//    Set<OrgUnit> getAllUserAccessibleOrganisationUnits();

    void updatePaths();

    void forceUpdatePaths();
}
