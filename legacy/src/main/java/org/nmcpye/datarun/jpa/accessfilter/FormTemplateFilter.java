package org.nmcpye.datarun.jpa.accessfilter;

import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.nmcpye.datarun.jpa.accessfilter.entity.UserExecutionContext;
import org.nmcpye.datarun.template.datatemplate.DataTemplate;
import org.nmcpye.datarun.iam.security.CurrentUserDetails;
import org.nmcpye.datarun.sharedkernal.apiquery.QueryRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

/**
 * @author Hamza Assada
 * @since 21/03/2025
 */
@Component
public class FormTemplateFilter extends DefaultJpaFilter<DataTemplate> {

    public static Specification<DataTemplate> isDeleted() {
        return (root, query, criteriaBuilder) -> criteriaBuilder.isTrue(root.get("deleted"));
    }

    public static Specification<DataTemplate> isActive() {
        return (root, query, criteriaBuilder) -> criteriaBuilder.notEqual(root.get("deleted"), true);
    }

    @Override
    public Specification<DataTemplate> getAccessSpecification(CurrentUserDetails user,
            QueryRequest queryRequest) {
        Specification<DataTemplate> spec = (root, query, cb) -> {
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
                    cb.equal(uec.get("entityType"), "DATA_TEMPLATE") // 'FormTemplate' implies DataTemplate access
            );

            return root.get("uid").in(sq);
        };

        if (queryRequest == null || !queryRequest.isIncludeDeleted()) {
            spec = spec.and(isActive());
        }
        return spec;
    }
}
