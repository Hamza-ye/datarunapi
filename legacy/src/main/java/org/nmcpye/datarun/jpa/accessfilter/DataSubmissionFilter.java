package org.nmcpye.datarun.jpa.accessfilter;

import org.nmcpye.datarun.datacapture.datasubmission.DataSubmission;
import org.nmcpye.datarun.iam.security.CurrentUserDetails;
import org.nmcpye.datarun.sharedkernal.apiquery.QueryRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

/**
 * @author Hamza Assada
 * @since 21/03/2025
 */
@Component
public class DataSubmissionFilter extends DefaultJpaFilter<DataSubmission> {

    public static Specification<DataSubmission> isActive() {
        return (root, query, criteriaBuilder) -> criteriaBuilder.notEqual(root.get("deleted"), true);
    }

    @Override
    public Specification<DataSubmission> getAccessSpecification(CurrentUserDetails user,
            QueryRequest queryRequest) {
        Specification<DataSubmission> spec = (root, query, cb) -> {
            if (user.isSuper()) {
                return cb.conjunction();
            } else {
                return cb.disjunction();
            }

//            if (query == null) {
//                return cb.conjunction();
//            }
//
//            // Path B: CQRS Subquery against UserExecutionContext for Form Access
//            // We only need to know if they have Form Access (which translates to Submission
//            // access)
//            Subquery<String> sq = query.subquery(String.class);
//            Root<UserExecutionContext> uec = sq.from(UserExecutionContext.class);
//
//            sq.select(uec.get("entityUid")).where(
//                    cb.equal(uec.get("userUid"), user.getUid()),
//                    cb.equal(uec.get("entityType"), "DATA_TEMPLATE")
//            // By default ANY access to the form implies SOME access to submissions
//            // If specific UPDATE/DELETE filtering is needed, it would filter by
//            // resolvedPermission here.
//            );
//
//            return root.get("templateUid").in(sq);
        };

        if (queryRequest == null || !queryRequest.isIncludeDeleted()) {
            spec = spec.and(isActive());
        }
        return spec;
    }
}
