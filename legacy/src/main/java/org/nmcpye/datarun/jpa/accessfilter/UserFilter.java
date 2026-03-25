package org.nmcpye.datarun.jpa.accessfilter;

import org.nmcpye.datarun.iam.user.User;
import org.nmcpye.datarun.iam.security.CurrentUserDetails;
import org.nmcpye.datarun.sharedkernal.apiquery.QueryRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

/**
 * @author Hamza Assada
 * @since 21/03/2025
 */
@Component
public class UserFilter extends DefaultJpaFilter<User> {
    @Override
    public Specification<User> getAccessSpecification(CurrentUserDetails user,
                                                      QueryRequest queryRequest) {
        return (root, query, cb) -> {
            if (user.isSuper()) {
                return cb.conjunction();
            } else {

                return cb.equal(root.get("login"), user.getUsername());
            }
        };
    }
}
