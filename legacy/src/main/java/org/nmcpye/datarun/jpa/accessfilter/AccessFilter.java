package org.nmcpye.datarun.jpa.accessfilter;

import org.nmcpye.datarun.iam.security.CurrentUserDetails;
import org.nmcpye.datarun.sharedkernal.IdentifiableObject;
import org.nmcpye.datarun.sharedkernal.apiquery.QueryRequest;
import org.springframework.data.jpa.domain.Specification;

/**
 * Generic Access Filter Interface, initial temporary
 * step before transitioning to ABAC
 *
 * @author Hamza Assada
 * @since 21/03/2025
 */
public interface AccessFilter<T extends IdentifiableObject<?>> {
    Class<T> getKlass();

    Specification<T> getAccessSpecification(CurrentUserDetails user, QueryRequest queryRequest);

    static <E extends IdentifiableObject<?>> Specification<E> createDefaultSpecification(CurrentUserDetails user) {
        return (root, query, criteriaBuilder) -> {
            if (user.isSuper()) {
                return criteriaBuilder.conjunction();
            } else {
                return criteriaBuilder.equal(root.get("createdBy"), user.getUsername());
            }
        };
    }
}
