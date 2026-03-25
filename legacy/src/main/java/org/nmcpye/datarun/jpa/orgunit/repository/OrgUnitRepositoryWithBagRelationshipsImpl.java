package org.nmcpye.datarun.jpa.orgunit.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.nmcpye.datarun.jpa.orgunit.OrgUnit;

import java.util.List;

/**
 * Utility repository to load bag relationships based on https://vladmihalcea.com/hibernate-multiplebagfetchexception/
 *
 * @author Hamza Assada 18/01/2022
 */
public class OrgUnitRepositoryWithBagRelationshipsImpl
    implements OrgUnitRepositoryWithBagRelationships {

    private static final String ID_PARAMETER = "id";
    private static final String LOGIN_PARAMETER = "login";
    private static final String ORG_UNITS_PARAMETER = "orgUnits";

    @PersistenceContext
    private EntityManager entityManager;

//    @Override
//    public Optional<OrgUnit> fetchBagRelationships(Optional<OrgUnit> orgUnit) {
//        return orgUnit.map(this::fetchAssignments);
//    }

//    @Override
//    public Page<OrgUnit> fetchBagRelationships(Page<OrgUnit> orgUnits) {
//        return new PageImpl<>(fetchBagRelationships(orgUnits.getContent()), orgUnits.getPageable(), orgUnits.getTotalElements());
//    }

    @Override
    public void updatePaths() {
        List<OrgUnit> organisationUnits = entityManager
            .createQuery(
                "select orgUnit from OrgUnit orgUnit " +
                    "where orgUnit.path is null or orgUnit.hierarchyLevel is null",
                OrgUnit.class
            )
            .getResultList();

        updatePaths(organisationUnits);
    }

    @Override
    public void forceUpdatePaths() {
        List<OrgUnit> organisationUnits = entityManager
            .createQuery(
                "select orgUnit from OrgUnit orgUnit ",
                OrgUnit.class
            )
            .getResultList();

        updatePaths(organisationUnits);
    }

    private void updatePaths(List<OrgUnit> organisationUnits) {

        int counter = 0;

        for (OrgUnit organisationUnit : organisationUnits) {
            organisationUnit.setPath(organisationUnit.getPath());
            organisationUnit.setHierarchyLevel(organisationUnit.getHierarchyLevel());

            entityManager.merge(organisationUnit);

            if ((counter % 400) == 0) {
                entityManager.flush();
            }

            counter++;
        }
    }

//    @Override
//    public List<OrgUnit> fetchBagRelationships(List<OrgUnit> orgUnits) {
//        return Optional.of(orgUnits).map(this::fetchAssignments).orElse(Collections.emptyList());
//    }
//
//    OrgUnit fetchAssignments(OrgUnit result) {
//        final String login = SecurityUtils.getCurrentUserLogin().orElse(null);
//
//        /// assignment.team.userInfo.login = ?#{authentication.name}
//        return entityManager
//            .createQuery(
//                "select orgUnit from OrgUnit orgUnit " +
//                    "left join fetch orgUnit.assignments ass " +
////                    "join Assignment assignment ON assignment.orgUnit = orgUnit " +
////                    "left join fetch orgUnit.children " +
//                    "where orgUnit.id = :id and ass.team.userInfo.login = :login",
//                OrgUnit.class
//            )
//            .setParameter(ID_PARAMETER, result.getId())
//            .setParameter(LOGIN_PARAMETER, login)
//            .getSingleResult();
//    }

//    List<OrgUnit> fetchAssignments(List<OrgUnit> orgUnits) {
//        HashMap<Object, Integer> order = new HashMap<>();
//        final String login = SecurityUtils.getCurrentUserLogin().orElse(null);
//        IntStream.range(0, orgUnits.size()).forEach(index -> order.put(orgUnits.get(index).getId(), index));
//        List<OrgUnit> result = entityManager
//            .createQuery(
//                "select orgUnit from OrgUnit orgUnit " +
//                    "left join fetch orgUnit.assignments ass " +
////                    "join Assignment assignment ON assignment.orgUnit = orgUnit " +
////                    "left join fetch orgUnit.children " +
//                    "where orgUnit in :orgUnits and ass.team.userInfo.login = :login",
//                OrgUnit.class
//            )
//            .setParameter(ORG_UNITS_PARAMETER, orgUnits)
//            .setParameter(LOGIN_PARAMETER, login)
//            .getResultList();
//        result.sort((o1, o2) -> Integer.compare(order.get(o1.getId()), order.get(o2.getId())));
//        return result;
//    }
}
