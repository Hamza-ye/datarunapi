package org.nmcpye.datarun.assignment.assignment.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.nmcpye.datarun.assignment.assignment.Assignment;

import java.util.List;

/**
 * Utility repository to load bag relationships based on https://vladmihalcea.com/hibernate-multiplebagfetchexception/
 */
public class AssignmentRepositoryWithBagRelationshipsImpl
    implements AssignmentRepositoryWithBagRelationships {

    private static final String ID_PARAMETER = "id";
    private static final String ORG_UNITS_PARAMETER = "assignments";

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public void updatePaths() {
        List<Assignment> assignments = entityManager
            .createQuery(
                "select assignment from Assignment assignment " +
                    "where assignment.path is null or assignment.hierarchyLevel is null",
                Assignment.class
            )
            .getResultList();

        updatePaths(assignments);
    }

    @Override
    public void forceUpdatePaths() {
        List<Assignment> assignments = entityManager
            .createQuery(
                "select assignment from Assignment assignment ",
                Assignment.class
            )
            .getResultList();

        updatePaths(assignments);
    }

    private void updatePaths(List<Assignment> assignments) {

        int counter = 0;

        for (Assignment assignment : assignments) {
            assignment.setPath(assignment.getPath());
            assignment.setHierarchyLevel(assignment.getHierarchyLevel());

            entityManager.merge(assignment);

            if ((counter % 400) == 0) {
                entityManager.flush();
            }

            counter++;
        }
    }
}
