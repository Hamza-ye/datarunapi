package org.nmcpye.datarun.jpa.accessfilter;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.nmcpye.datarun.jpa.accessfilter.entity.UserExecutionContext;
import org.nmcpye.datarun.assignment.activity.Activity;
import org.nmcpye.datarun.assignment.assignment.Assignment;
import org.nmcpye.datarun.iam.team.Team;
import org.nmcpye.datarun.iam.security.CurrentUserDetails;
import org.nmcpye.datarun.sharedkernal.apiquery.QueryRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

/**
 * @author Hamza Assada
 * @since 21/03/2025
 */
@Component
public class AssignmentFilter extends DefaultJpaFilter<Assignment> {
    public static Specification<Assignment> isEnabled() {
        return (root, query, cb) -> {
            Join<Assignment, Activity> activityJoin = root.join("activity", JoinType.LEFT);
            Join<Assignment, Team> teamJoin = root.join("team", JoinType.LEFT);

            Predicate activityNotDisabled = cb.isFalse(activityJoin.get("disabled"));
            Predicate teamNotDisabled = cb.isFalse(teamJoin.get("disabled"));

            return cb.and(teamNotDisabled, activityNotDisabled);
        };
    }

    @Override
    public Specification<Assignment> getAccessSpecification(CurrentUserDetails user,
            QueryRequest queryRequest) {
        Specification<Assignment> spec = (root, query, cb) -> {
            if (user.isSuper()) {
                return cb.conjunction();
            }

            if (query == null) {
                return cb.conjunction();
            }

            Join<Assignment, Team> assignmentJoin = root.join("team", JoinType.INNER);

            // Path B: CQRS Subquery against UserExecutionContext
            Subquery<String> sq = query.subquery(String.class);
            Root<UserExecutionContext> uec = sq.from(UserExecutionContext.class);

            sq.select(uec.get("entityUid")).where(
                    cb.equal(uec.get("userUid"), user.getUid()),
                    cb.equal(uec.get("entityType"), "TEAM"));

            return assignmentJoin.get("uid").in(sq);
        };

        if (queryRequest == null || !queryRequest.isIncludeDisabled()) {
            spec = spec.and(isEnabled());
        }
        return spec;
    }
}
