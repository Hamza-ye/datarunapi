package org.nmcpye.datarun.jpa.accessfilter;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.nmcpye.datarun.jpa.accessfilter.entity.UserExecutionContext;
import org.nmcpye.datarun.assignment.activity.Activity;
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
public class TeamFilter extends DefaultJpaFilter<Team> {
    public static Specification<Team> isEnabled() {
        return (root, query, criteriaBuilder) -> {
            Join<Team, Activity> activityJoin = root.join("activity", JoinType.LEFT);

            Predicate teamNotDisabled = criteriaBuilder.isFalse(root.get("disabled"));
            Predicate activityNotDisabled = criteriaBuilder.isFalse(activityJoin.get("disabled"));

            return criteriaBuilder.and(teamNotDisabled, activityNotDisabled);
        };
    }

    @Override
    public Specification<Team> getAccessSpecification(CurrentUserDetails user,
            QueryRequest queryRequest) {
        Specification<Team> spec = (root, query, cb) -> {
            if (user.isSuper()) {
                return cb.conjunction();
            }

            if (query == null) {
                return cb.conjunction();
            }

            // Path B: CQRS Subquery against UserExecutionContext
            Subquery<String> sq = query.subquery(String.class);
            Root<UserExecutionContext> uec = sq.from(UserExecutionContext.class);

            sq.select(uec.get("entityUid")).where(
                    cb.equal(uec.get("userUid"), user.getUid()),
                    cb.equal(uec.get("entityType"), "TEAM"));

            return root.get("uid").in(sq);
        };

        if (queryRequest == null || !queryRequest.isIncludeDisabled()) {
            spec = spec.and(isEnabled());
        }
        return spec;
    }
}
