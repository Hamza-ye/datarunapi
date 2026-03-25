package org.nmcpye.datarun.jpa.orgunit.repository;

/**
 * @author Hamza Assada 18/01/2022
 */
public interface OrgUnitRepositoryWithBagRelationships {
//    Optional<OrgUnit> fetchBagRelationships(Optional<OrgUnit> orgUnit);

//    List<OrgUnit> fetchBagRelationships(List<OrgUnit> orgUnits);

//    Page<OrgUnit> fetchBagRelationships(Page<OrgUnit> orgUnits);

    void updatePaths();

    void forceUpdatePaths();
}
