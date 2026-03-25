package org.nmcpye.datarun.jpa.accessfilter;

import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.nmcpye.datarun.jpa.accessfilter.entity.UserExecutionContext;
import org.nmcpye.datarun.assignment.activity.Activity;
import org.nmcpye.datarun.iam.security.CurrentUserDetails;
import org.nmcpye.datarun.sharedkernal.apiquery.QueryRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

/**
 * @author Hamza Assada
 * @since 21/03/2025
 */
@Component
public class ActivityFilter extends DefaultJpaFilter<Activity> {
    public static Specification<Activity> isEnabled() {
        return (root, query, criteriaBuilder) -> criteriaBuilder.isFalse(root.get("disabled"));
    }

    @Override
    public Specification<Activity> getAccessSpecification(CurrentUserDetails user,
            QueryRequest queryRequest) {

        Specification<Activity> spec = (root, query, cb) -> {
            if (user.isSuper()) {
                return cb.conjunction();
            }

            if (query == null) {
                return cb.conjunction();
            }

            // Path B: CQRS Subquery against UserExecutionContext
            // SELECT entity_uid FROM user_execution_context WHERE user_uid = ? AND
            // entity_type = 'ACTIVITY'
            Subquery<String> sq = query.subquery(String.class);
            Root<UserExecutionContext> uec = sq.from(UserExecutionContext.class);

            sq.select(uec.get("entityUid")).where(
                    cb.equal(uec.get("userUid"), user.getUid()),
                    cb.equal(uec.get("entityType"), "ACTIVITY"));

            return root.get("uid").in(sq);
        };

        if (queryRequest == null || !queryRequest.isIncludeDisabled()) {
            spec = spec.and(isEnabled());
        }
        return spec;
    }
}
