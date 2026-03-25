package org.nmcpye.datarun.jpa.accessfilter;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.nmcpye.datarun.jpa.accessfilter.entity.UserExecutionContext;
import org.nmcpye.datarun.assignment.assignment.Assignment;
import org.nmcpye.datarun.jpa.orgunit.OrgUnit;
import org.nmcpye.datarun.iam.security.CurrentUserDetails;
import org.nmcpye.datarun.sharedkernal.apiquery.QueryRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

/**
 * @author Hamza Assada
 * @since 21/03/2025
 */
@Component
public class OrgUnitFilter extends DefaultJpaFilter<OrgUnit> {

    @Override
    public Specification<OrgUnit> getAccessSpecification(CurrentUserDetails user,
            QueryRequest queryRequest) {
        return (root, query, cb) -> {
            if (user.isSuper()) {
                return cb.conjunction();
            }

            if (query == null) {
                return cb.conjunction();
            }

            // Path B: An OrgUnit is accessible if it is linked to an Assignment
            // that the user has access to via the CQRS UserExecutionContext.
            // OrgUnit <- Assignment -> UserExecutionContext

            Subquery<Long> sqAssignment = query.subquery(Long.class);
            Root<Assignment> assignmentRoot = sqAssignment.from(Assignment.class);
            Join<Assignment, OrgUnit> orgUnitJoin = assignmentRoot.join("orgUnit", JoinType.INNER);

            Subquery<String> sqUec = sqAssignment.subquery(String.class);
            Root<UserExecutionContext> uec = sqUec.from(UserExecutionContext.class);
            sqUec.select(uec.get("entityUid")).where(
                    cb.equal(uec.get("userUid"), user.getUid()),
                    cb.equal(uec.get("entityType"), "TEAM") // Team implies Assignment access for Phase 1 flattening
            );

            // Fetch the OrgUnit IDs that match accessible Assignments (which match
            // accessible Teams)
            sqAssignment.select(orgUnitJoin.get("id"))
                    .where(assignmentRoot.join("team").get("uid").in(sqUec));

            return root.get("id").in(sqAssignment);
        };
    }
}
