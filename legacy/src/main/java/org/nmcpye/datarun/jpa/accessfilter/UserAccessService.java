package org.nmcpye.datarun.jpa.accessfilter;

import org.nmcpye.datarun.iam.security.CurrentUserDetails;
import org.nmcpye.datarun.sharedkernal.IdentifiableObject;
import org.nmcpye.datarun.sharedkernal.apiquery.QueryRequest;
import org.springframework.data.jpa.domain.Specification;

/**
 * @author Hamza Assada
 * @since 20/03/2025
 */
public interface UserAccessService {
    <T extends IdentifiableObject<?>> Specification<T> readSpec(Class<T> klass, CurrentUserDetails user, QueryRequest queryRequest);
}
