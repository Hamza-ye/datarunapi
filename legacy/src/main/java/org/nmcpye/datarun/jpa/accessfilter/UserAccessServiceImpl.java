package org.nmcpye.datarun.jpa.accessfilter;

import org.nmcpye.datarun.iam.security.CurrentUserDetails;
import org.nmcpye.datarun.sharedkernal.IdentifiableObject;
import org.nmcpye.datarun.sharedkernal.apiquery.QueryRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

/**
 * @author Hamza Assada
 * @since 20/03/2025
 */
@Service
public class UserAccessServiceImpl
    implements UserAccessService {
    private final AccessFilterRegistry accessFilterRegistry;

    public UserAccessServiceImpl(AccessFilterRegistry accessFilterRegistry) {
        this.accessFilterRegistry = accessFilterRegistry;
    }

    @Override
    public <T extends IdentifiableObject<?>> Specification<T> readSpec(Class<T> klass, CurrentUserDetails user,
                                                                       QueryRequest queryRequest) {
        return accessFilterRegistry.getSpecification(klass, user, queryRequest);
    }
}
